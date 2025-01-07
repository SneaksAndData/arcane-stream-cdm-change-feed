package com.sneaksanddata.arcane.cdm_change_feed
package services.streaming.processors

import com.sneaksanddata.arcane.framework.models.ArcaneType.*
import com.sneaksanddata.arcane.framework.models.settings.GroupingSettings
import com.sneaksanddata.arcane.framework.models.{ArcaneType, DataCell, DataRow}
import com.sneaksanddata.arcane.framework.services.streaming.base.BatchProcessor
import zio.stream.ZPipeline
import zio.{Chunk, ZIO, ZLayer}

import scala.concurrent.duration.Duration

/**
 * The batch processor implementation that converts a lazy DataBatch to a Chunk of DataRow.
 * @param groupingSettings The grouping settings.
 */
class CdmGroupingProcessor(groupingSettings: GroupingSettings) extends BatchProcessor[LazyList[DataRow], Chunk[DataRow]]:

  /**
   * Processes the incoming data.
   *
   * @return ZPipeline (stream source for the stream graph).
   */
  def process: ZPipeline[Any, Throwable, LazyList[DataRow], Chunk[DataRow]] = ZPipeline
    .map[LazyList[DataRow], Chunk[DataRow]](list => Chunk.fromIterable(list))
    .flattenChunks
    .map(row => toTypedRow(row))
    .groupedWithin(groupingSettings.rowsPerGroup, groupingSettings.groupingInterval)

  private def toTypedRow(row: DataRow): DataRow = row map { cell =>
    DataCell(cell.name, cell.Type, convertType(cell.Type, cell.value))
  }

  private def convertType(arcaneType: ArcaneType, value: Any): Any =
    value match
      case None => null
      case Some(v) => convertSome(arcaneType, v)

  private def convertSome(arcaneType: ArcaneType, value: Any): Any = arcaneType match
    case LongType => value.toString.toLong
    case ByteArrayType => value.toString.getBytes
    case BooleanType => value.toString.toBoolean
    case StringType => value.toString
    case DateType  => java.sql.Date.valueOf(value.toString)
    case TimestampType => null //java.sql.Timestamp.valueOf(value.toString) // TODO
    case DateTimeOffsetType => java.time.OffsetDateTime.parse(value.toString)
    case BigDecimalType => BigDecimal(value.toString)
    case DoubleType => value.toString.toDouble
    case IntType => value.toString.toInt
    case FloatType => value.toString.toFloat
    case ShortType => value.toString.toShort
    case TimeType => java.sql.Time.valueOf(value.toString)

/**
 * The companion object for the LazyOutputDataProcessor class.
 */
object CdmGroupingProcessor:

  /**
   * The ZLayer that creates the LazyOutputDataProcessor.
   */
  val layer: ZLayer[GroupingSettings, Nothing, CdmGroupingProcessor] =
    ZLayer {
      for
        settings <- ZIO.service[GroupingSettings]
      yield CdmGroupingProcessor(settings)
    }

  def apply(groupingSettings: GroupingSettings): CdmGroupingProcessor =
    require(groupingSettings.rowsPerGroup > 0, "Rows per group must be greater than 0")
    require(!groupingSettings.groupingInterval.equals(Duration.Zero), "groupingInterval must be greater than 0")
    new CdmGroupingProcessor(groupingSettings)
