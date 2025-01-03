package com.sneaksanddata.arcane.cdm_change_feed
package services.cdm

import models.app.CdmStreamContext
import services.graph_builder.{BackfillDataGraphBuilder, VersionedDataGraphBuilder}

import com.sneaksanddata.arcane.framework.models.ArcaneSchema
import com.sneaksanddata.arcane.framework.models.app.StreamContext
import com.sneaksanddata.arcane.framework.models.cdm.{SimpleCdmEntity, SimpleCdmModel, given_Conversion_SimpleCdmEntity_ArcaneSchema}
import com.sneaksanddata.arcane.framework.services.base.SchemaProvider
import com.sneaksanddata.arcane.framework.services.cdm.CdmTableSettings
import com.sneaksanddata.arcane.framework.services.mssql.given_CanAdd_ArcaneSchema
import com.sneaksanddata.arcane.framework.services.storage.models.azure.AzureBlobStorageReader
import zio.{ZIO, ZLayer}

import scala.concurrent.Future

class CdmSchemaProvider(azureBlobStorageReader: AzureBlobStorageReader, tableLocation: String, tableName: String) extends SchemaProvider[ArcaneSchema]:
  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  override def getSchema: Future[SchemaType] = SimpleCdmModel(tableLocation, azureBlobStorageReader).flatMap(toArcaneSchema)

  override def empty: SchemaType = ArcaneSchema.empty()
  
  private def toArcaneSchema(simpleCdmModel: SimpleCdmModel): Future[ArcaneSchema] =
    simpleCdmModel.entities.find(_.name == tableName) match
      case None => Future.failed(new Exception(s"Table $tableName not found in model $tableLocation"))
      case Some(entity) => Future.successful(entity)

object CdmSchemaProvider:
  
  private type Environment = AzureBlobStorageReader & CdmTableSettings
  
  val layer: ZLayer[Environment, Nothing, CdmSchemaProvider] = 
      ZLayer {
        for
          context <- ZIO.service[CdmTableSettings]
          settings <- ZIO.service[AzureBlobStorageReader]
        yield CdmSchemaProvider(settings, context.name, context.rootPath)
      }
