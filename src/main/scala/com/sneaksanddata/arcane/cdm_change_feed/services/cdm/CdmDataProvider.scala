package com.sneaksanddata.arcane.cdm_change_feed
package services.cdm

import services.streaming.base.VersionedDataProvider
import com.sneaksanddata.arcane.cdm_change_feed.models.app.AzureConnectionSettings
import com.sneaksanddata.arcane.framework.models.DataRow
import com.sneaksanddata.arcane.framework.services.cdm.{CdmTable, CdmTableSettings}
import com.sneaksanddata.arcane.framework.services.mssql.MsSqlConnection.BackfillBatch
import com.sneaksanddata.arcane.framework.services.storage.models.azure.AzureBlobStorageReader
import com.sneaksanddata.arcane.framework.services.streaming.base.BackfillDataProvider
import zio.{Task, ZIO, ZLayer}

import java.time.{Duration, OffsetDateTime, ZoneOffset}
import scala.concurrent.Future

//class Ctable extends CdmTable:
//  def snapshot(time: Option[OffsetDateTime]): Task[LazyList[DataRow]] =
//    // list all matching blobs
//    Future.sequence(getListPrefixes(startDate, endDate)
//        .flatMap(prefix => reader.listPrefixes(storagePath + prefix))
//        .flatMap(prefix => reader.listBlobs(storagePath + prefix.name + name))
//        // exclude any files other than CSV
//        .collect {
//          case blob if blob.name.endsWith(".csv") => reader.getBlobContent(storagePath + blob.name)
//        })
//      .map(_.flatMap(content => replaceQuotedNewlines(content).split('\n').map(implicitly[DataRow](_, schema))))
//      .map(LazyList.from)

/**
 * A data provider that reads the changes from the Microsoft SQL Server.
 * @param msSqlConnection The connection to the Microsoft SQL Server.
 */
class CdmDataProvider(cdmTable: CdmTable) extends VersionedDataProvider[OffsetDateTime, LazyList[DataRow]] with BackfillDataProvider:

  override def extractVersion(dataBatch: LazyList[DataRow]): Option[OffsetDateTime] = Some(OffsetDateTime.now()) // TODO: this should return the version from the last data row

  override def requestBackfill: Task[BackfillBatch] = ???

  override def requestChanges(previousVersion: Option[OffsetDateTime], lookBackInterval: Duration): Task[LazyList[DataRow]] =
    val time = previousVersion.getOrElse(OffsetDateTime.now().minusHours(12))
    ZIO.fromFuture(_ => cdmTable.snapshot(Some(time)))

/**
 * The companion object for the MsSqlDataProvider class.
 */
object CdmDataProvider:
  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  type Environment = AzureConnectionSettings
    & CdmTableSettings
    & AzureBlobStorageReader
    & CdmSchemaProvider

  /**
   * The ZLayer that creates the CdmDataProvider.
   */
  val layer: ZLayer[Environment, Throwable, CdmDataProvider] =
    ZLayer {
      for {
        _ <- ZIO.log("Creating the CDM data provider")
        connectionSettings <- ZIO.service[AzureConnectionSettings]
        tableSettings <- ZIO.service[CdmTableSettings]
        reader <- ZIO.service[AzureBlobStorageReader]
        schemaProvider <- ZIO.service[CdmSchemaProvider]
        l <- ZIO.fromFuture(_ => schemaProvider.getEntity)
        cdmTable = CdmTable(tableSettings, l, reader)
      } yield CdmDataProvider(cdmTable)
    }
