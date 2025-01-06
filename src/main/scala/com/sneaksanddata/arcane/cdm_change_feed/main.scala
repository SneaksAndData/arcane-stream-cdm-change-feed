package com.sneaksanddata.arcane.cdm_change_feed

import models.app.{AzureConnectionSettings, CdmStreamContext}
import services.StreamGraphBuilderFactory
import services.cdm.{CdmDataProvider, CdmSchemaProvider}
import services.streaming.processors.CdmGroupingProcessor
import com.azure.storage.common.StorageSharedKeyCredential
import com.sneaksanddata.arcane.cdm_change_feed.services.streaming.consumers.IcebergSynapseConsumer
import com.sneaksanddata.arcane.framework.models.DataRow
import com.sneaksanddata.arcane.framework.models.app.StreamContext
import com.sneaksanddata.arcane.framework.models.settings.{GroupingSettings, VersionedDataGraphBuilderSettings}
import com.sneaksanddata.arcane.framework.services.app.base.{StreamLifetimeService, StreamRunnerService}
import com.sneaksanddata.arcane.framework.services.app.{PosixStreamLifetimeService, StreamRunnerServiceImpl}
import com.sneaksanddata.arcane.framework.services.consumers.JdbcConsumer
import com.sneaksanddata.arcane.framework.services.lakehouse.IcebergS3CatalogWriter
import com.sneaksanddata.arcane.framework.services.storage.models.azure.AzureBlobStorageReader
import com.sneaksanddata.arcane.framework.services.streaming.base.{BatchProcessor, StreamGraphBuilder}
import com.sneaksanddata.arcane.framework.services.streaming.consumers.{IcebergBackfillConsumer, IcebergStreamingConsumer}
import com.sneaksanddata.arcane.framework.services.streaming.processors.{BackfillGroupingProcessor, MergeProcessor}
import zio.{ZIO, ZIOAppDefault, ZLayer}
import zio.*


object main extends ZIOAppDefault {

//  private val loggingProprieties = Enricher("Application", "Arcane.Stream.Scala")
//    ++ Enricher("App", "Arcane.Stream.Scala")
//    ++ Enricher.fromEnvironment("APPLICATION_VERSION", "0.0.0")
//
//  override val bootstrap: ZLayer[Any, Nothing, Unit] = SLF4J.slf4j(
//    LogFormat.make{ (builder, _, _, _, line, _, _, _, _) =>
//      loggingProprieties.enrichLoggerWith(builder.appendKeyValue)
//      loggingProprieties.enrichLoggerWith(MDC.put)
//      builder.appendText(line())
//    }
//  )

  private val appLayer  = for
    _ <- ZIO.log("Application starting")
    context <- ZIO.service[StreamContext].debug("initialized stream context")
    streamRunner <- ZIO.service[StreamRunnerService].debug("initialized stream runner")
    _ <- streamRunner.run
  yield ()

  val storageExplorerLayer: ZLayer[AzureConnectionSettings, Nothing, AzureBlobStorageReader] = ZLayer {
   for {
     connectionOptions <- ZIO.service[AzureConnectionSettings]
     credentials = StorageSharedKeyCredential(connectionOptions.account, connectionOptions.accessKey)
   } yield AzureBlobStorageReader(connectionOptions.account, connectionOptions.endpoint, credentials)
  }

  @main
  def run: ZIO[Any, Throwable, Unit] =
    appLayer.provide(
      storageExplorerLayer,
      CdmDataProvider.layer,
      CdmSchemaProvider.layer,
      CdmStreamContext.layer,
      PosixStreamLifetimeService.layer,
      StreamRunnerServiceImpl.layer,
      StreamGraphBuilderFactory.layer,
      BackfillGroupingProcessor.layer,
      IcebergS3CatalogWriter.layer,
      IcebergSynapseConsumer.layer,
      MergeProcessor.layer,
      JdbcConsumer.layer,
      CdmGroupingProcessor.layer,
      IcebergBackfillConsumer.layer)
    .orDie
}

