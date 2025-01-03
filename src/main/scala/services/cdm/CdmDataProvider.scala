package com.sneaksanddata.arcane.cdm_change_feed
package services.cdm

import services.streaming.base.VersionedDataProvider

import com.sneaksanddata.arcane.framework.models.DataRow
import com.sneaksanddata.arcane.framework.services.mssql.MsSqlConnection.BackfillBatch
import com.sneaksanddata.arcane.framework.services.streaming.base.BackfillDataProvider
import zio.{Task, ZIO, ZLayer}

import java.time.{Duration, OffsetDateTime}


/**
 * A data provider that reads the changes from the Microsoft SQL Server.
 * @param msSqlConnection The connection to the Microsoft SQL Server.
 */
class CdmDataProvider extends VersionedDataProvider[OffsetDateTime, LazyList[DataRow]] with BackfillDataProvider:

  override def extractVersion(dataBatch: LazyList[DataRow]): Option[OffsetDateTime] = ???

  override def requestBackfill: Task[BackfillBatch] = ???

  override def requestChanges(previousVersion: Option[OffsetDateTime], lookBackInterval: Duration): Task[LazyList[DataRow]] = ???

/**
 * The companion object for the MsSqlDataProvider class.
 */
object CdmDataProvider:

  /**
   * The ZLayer that creates the MsSqlDataProvider.
   */
  val layer: ZLayer[Any, Nothing, CdmDataProvider] =
    ZLayer {
      ZIO.succeed(new CdmDataProvider())
    }
