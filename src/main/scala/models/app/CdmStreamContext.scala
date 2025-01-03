package com.sneaksanddata.arcane.cdm_change_feed
package models.app

import com.sneaksanddata.arcane.framework.models.app.StreamContext
import com.sneaksanddata.arcane.framework.models.settings.{GroupingSettings, SinkSettings, VersionedDataGraphBuilderSettings}
import com.sneaksanddata.arcane.framework.services.cdm.CdmTableSettings
import com.sneaksanddata.arcane.framework.services.consumers.JdbcConsumerOptions
import com.sneaksanddata.arcane.framework.services.lakehouse.{IcebergCatalogCredential, S3CatalogFileIO}
import com.sneaksanddata.arcane.framework.services.lakehouse.base.IcebergCatalogSettings
import com.sneaksanddata.arcane.framework.services.mssql.ConnectionOptions
import zio.ZLayer
import zio.json.*

import java.time.Duration

trait AzureConnectionSettings:
  val endpoint: String
  val container: String
  val account: String
  val accessKey: String


/**
 * The context for the SQL Server Change Tracking stream.
 * @param spec The stream specification
 */
case class CdmStreamContext(spec: StreamSpec) extends StreamContext
  with GroupingSettings
  with IcebergCatalogSettings
  with JdbcConsumerOptions
  with VersionedDataGraphBuilderSettings
  with AzureConnectionSettings
  with SinkSettings:

  override val rowsPerGroup: Int = spec.rowsPerGroup
  override val lookBackInterval: Duration = Duration.ofSeconds(spec.lookBackInterval)
  override val changeCaptureInterval: Duration = Duration.ofSeconds(spec.changeCaptureIntervalSeconds)
  override val groupingInterval: Duration = Duration.ofSeconds(spec.groupingIntervalSeconds)

  override val namespace: String = spec.catalogSettings.namespace
  override val warehouse: String = spec.catalogSettings.warehouse
  override val catalogUri: String = spec.catalogSettings.catalogUri

  override val additionalProperties: Map[String, String] = IcebergCatalogCredential.oAuth2Properties
  override val s3CatalogFileIO: S3CatalogFileIO = S3CatalogFileIO

  override val stagingLocation: Option[String] = spec.stagingLocation

  @jsonExclude
  val connectionString: String = sys.env("ARCANE_CONNECTIONSTRING")

  @jsonExclude
  override val connectionUrl: String = sys.env("ARCANE_FRAMEWORK__MERGE_SERVICE_CONNECTION_URI")

  override def toString: String = this.toJsonPretty

  /**
   * The target table to write the data.
   */
  override val sinkLocation: String = spec.sinkLocation

  override val endpoint: String = sys.env("ARCANE_FRAMEWORK__STORAGE_ENDPOINT")
  override val container: String = sys.env("ARCANE_FRAMEWORK__STORAGE_CONTAINER")
  override val account: String = sys.env("ARCANE_FRAMEWORK__STORAGE_ACCOUNT")
  override val accessKey: String = sys.env("ARCANE_FRAMEWORK__STORAGE_ACCESS_KEY")


given Conversion[CdmStreamContext, CdmTableSettings] with
  def apply(context: CdmStreamContext): CdmTableSettings = CdmTableSettings(context.spec.name, context.spec.baseLocation)

object CdmStreamContext {
  implicit val icebergSettingsDecoder: JsonDecoder[CatalogSettings] = DeriveJsonDecoder.gen[CatalogSettings]
  implicit val streamSpecDecoder: JsonDecoder[StreamSpec] = DeriveJsonDecoder.gen[StreamSpec]

  implicit val icebergSettingsEncoder: JsonEncoder[CatalogSettings] = DeriveJsonEncoder.gen[CatalogSettings]
  implicit val specEncoder: JsonEncoder[StreamSpec] = DeriveJsonEncoder.gen[StreamSpec]
  implicit val contextEncoder: JsonEncoder[CdmStreamContext] = DeriveJsonEncoder.gen[CdmStreamContext]

  type Environment = StreamContext
    & CdmTableSettings
    & GroupingSettings
    & VersionedDataGraphBuilderSettings
    & IcebergCatalogSettings
    & JdbcConsumerOptions
    & SinkSettings
    & AzureConnectionSettings

  /**
   * The ZLayer that creates the VersionedDataGraphBuilder.
   */
  val layer: ZLayer[Any, Throwable, Environment] =
    sys.env.get("STREAMCONTEXT__SPEC") map { raw =>
      val spec = raw.fromJson[StreamSpec] match {
        case Left(error) => throw new Exception(s"Failed to decode the stream context: $error")
        case Right(value) => value
      }
      val context = CdmStreamContext(spec)
      ZLayer.succeed(context) ++ ZLayer.succeed[CdmTableSettings](context)
    } getOrElse {
      ZLayer.fail(new Exception("The stream context is not specified."))
    }
}
