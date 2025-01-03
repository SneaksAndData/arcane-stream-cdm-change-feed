package com.sneaksanddata.arcane.cdm_change_feed
package models.app

/**
  * The configuration of Iceberg sink.
 */
case class CatalogSettings(namespace: String, warehouse: String, catalogUri: String)

/**
 * The specification for the stream.
 *
 * @param name The name of the CDM table
 * @param baseLocation The entity base location
 * @param rowsPerGroup The number of rows per group in the staging table
 * @param groupingIntervalSeconds The grouping interval in seconds
 * @param groupsPerFile The number of groups per file
 * @param lookBackInterval The look back interval in seconds
 * @param changeCaptureIntervalSeconds The change capture interval in seconds
 * @param partitionExpression Partition expression for partitioning the data in the staging table (optional)
 */
case class StreamSpec(name: String,
                      baseLocation: String,
                     
                      // Grouping settings
                      rowsPerGroup: Int,
                      groupingIntervalSeconds: Int,
                      groupsPerFile: Int,
                      lookBackInterval: Int,
                     
                      // Timeouts
                      changeCaptureIntervalSeconds: Int,

                      // Iceberg settings
                      catalogSettings: CatalogSettings,

                      stagingLocation: Option[String],
                      sinkLocation: String,
                      partitionExpression: Option[String])


