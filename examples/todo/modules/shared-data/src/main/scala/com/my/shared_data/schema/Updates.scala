package com.my.shared_data.schema

import org.tessellation.currency.dataApplication.DataUpdate

import derevo.circe.magnolia.{decoder, encoder}
import derevo.derive

object Updates {

  @derive(decoder, encoder)
  sealed abstract class TodoUpdate extends DataUpdate

  @derive(decoder, encoder)
  final case class CreateTask(
  modelID:                String,
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
  ) extends TodoUpdate
}
