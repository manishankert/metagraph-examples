package com.my.shared_data.schema

import cats.effect.Async
import cats.implicits.toFunctorOps

import org.tessellation.schema.SnapshotOrdinal
import org.tessellation.schema.address.Address
import org.tessellation.security.Hasher

import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto.{deriveConfiguredDecoder, deriveConfiguredEncoder}
import io.circe.{Decoder, Encoder}

//@derive(decoder, encoder)
//final case class TaskRecord(
//  id:                 String,
//  creationOrdinal:    SnapshotOrdinal,
//  lastUpdatedOrdinal: SnapshotOrdinal,
//  dueDateEpochMilli:  Long,
//  status:             TaskStatus,
//  reporter:           Address
//)

@derive(decoder, encoder)
final case class TaskRecord(
                      modelID:                String,
                      creationOrdinal:    SnapshotOrdinal,
                      lastUpdatedOrdinal: SnapshotOrdinal,
                      model:                  String,
                      typeOfmodel:            String,
                      primaryUseCase:         String,
                      keyFeatures:            String,
                      exampleOfApplication:   String,
                      timeStampCreation:      String,
                      timeStampCompletion:    String,
                      timeStampUpdate:        String,
                      ageOfModel:             Int,
                      versionControl:         Float,
                      watermarked:            Boolean
                    )