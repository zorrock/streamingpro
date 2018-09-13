package streaming.dsl.mmlib.algs

import org.apache.spark.sql._
import org.apache.spark.sql.expressions.UserDefinedFunction
import org.apache.spark.sql.types.{ArrayType, StringType, StructType}
import _root_.streaming.dsl.mmlib.SQLAlg
import _root_.streaming.dsl.mmlib.algs.meta.MapValuesMeta


class SQLMapValues extends SQLAlg with Functions {
  override def train(df: DataFrame, path: String, params: Map[String, String]): DataFrame = {
    val spark = df.sparkSession
    import spark.implicits._
    val metaPath = MetaConst.getMetaPath(path)
    val dataPath = MetaConst.getDataPath(path)
    saveTraningParams(df.sparkSession, params + ("path" -> path), metaPath)
    val inputCol = params.get("inputCol")
    val outputCol = params.get("outputCol")
    val mapMissingTo = params.get("mapMissingTo")
    require(mapMissingTo.isDefined)
    require(inputCol.isDefined, "inputCol should be configured!")
    require(outputCol.isDefined, "outputCol should be configured!")

    // validate mapMissingTo
    val mapMissingToValue = df.filter(row => {
      row.getAs[String](inputCol.get) == mapMissingTo.get
    }).collect()

    require(mapMissingToValue.size == 1, s"can't find or find multi ${mapMissingTo.get} in giving table!")

    // save dictionary
    val toSaveCols = Array(inputCol.get, outputCol.get)

    df.select(toSaveCols.map(new Column(_)): _*)
      .write
      .mode(SaveMode.Overwrite)
      .parquet(dataPath)

    // save train metadata
    val meta = MapValuesMeta(inputCol.get, outputCol.get, mapMissingTo.get)
    spark.createDataFrame(Seq(meta))
      .write
      .mode(SaveMode.Overwrite)
      .parquet(metaPath)
    emptyDataFrame()(df)
  }

  override def load(sparkSession: SparkSession, path: String, params: Map[String, String]): Any = {

    import sparkSession.implicits._

    // load dictionary and train parameters.
    val dataPath = MetaConst.getDataPath(path)

    val dict = sparkSession.read.parquet(dataPath)

    val metaPath = MetaConst.getMetaPath(path)

    val meta = sparkSession.read.parquet(metaPath).as[MapValuesMeta].collect().head
    (dict, meta)
  }

  override def predict(sparkSession: SparkSession,
                       _model: Any,
                       name: String,
                       params: Map[String, String]): UserDefinedFunction = {
    val (dict, meta) = _model.asInstanceOf[(DataFrame, MapValuesMeta)]

    val outputDataType = dict.schema.fields.filter(st => meta.outputCol == st.name).head.dataType

    println("wuheyi" + outputDataType)
    val a = ArrayType(outputDataType)


    val mapMissingToValue = dict.filter(row => {
      row.getAs[String](meta.inputCol) == meta.mapMissingTo
    }).collect()
      .head
      .getAs[Any](meta.outputCol)

    val dictionary = dict.collect().map(f => {
      val key = f.getAs[String](meta.inputCol)
      val value = f.getAs[Any](meta.outputCol)
      (key, value)
    }).toMap

    val defaultvalue = sparkSession.sparkContext.broadcast(mapMissingToValue)
    val dictbc = sparkSession.sparkContext.broadcast(dictionary)

    val fArray = (keys: Seq[String]) => {
      keys.map(key => {
        dictbc.value.getOrElse(key, defaultvalue.value)
      })
    }

    val audf = UserDefinedFunction(fArray, ArrayType(outputDataType), Some(Seq(ArrayType(StringType))))

    sparkSession.udf.register(name + "_array", audf)

    val f = (key: String) => {
      dictbc.value.getOrElse(key, defaultvalue.value)
    }
    UserDefinedFunction(f, outputDataType, Some(Seq(StringType)))
  }
}
