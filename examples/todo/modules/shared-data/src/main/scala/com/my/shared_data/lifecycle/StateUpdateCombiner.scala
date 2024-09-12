package com.my.shared_data.lifecycle

import cats.effect.Async
import cats.implicits.{toFlatMapOps, toFunctorOps}

import org.tessellation.currency.dataApplication.{DataState, L0NodeContext}
import org.tessellation.ext.cats.syntax.next.catsSyntaxNext
import org.tessellation.schema.SnapshotOrdinal
import org.tessellation.security.signature.Signed
import org.tessellation.security.{Hasher, SecurityProvider}

import com.my.shared_data.schema.Updates.TodoUpdate
import com.my.shared_data.schema._

import monocle.Monocle.toAppliedFocusOps

trait StateUpdateCombiner[F[_], U, T] {
  def insert(state: T, signedUpdate: Signed[U])(implicit ctx: L0NodeContext[F]): F[T]
}

object StateUpdateCombiner {

  type TX = TodoUpdate
  type DS = DataState[OnChain, CalculatedState]

  def make[F[_]: Async: SecurityProvider: Hasher]: StateUpdateCombiner[F, TX, DS] =
    new StateUpdateCombiner[F, TX, DS] {

      override def insert(state: DS, signedUpdate: Signed[TX])(implicit ctx: L0NodeContext[F]): F[DS] =
        signedUpdate.value match {
          case u: Updates.CreateTask   => createTask(Signed(u, signedUpdate.proofs), state, ctx)
        }

      private def createTask(update: Signed[Updates.CreateTask], inState: DS, ctx: L0NodeContext[F]): F[DS] =
        for {
          currentOrdinal <- ctx.getLastCurrencySnapshot
            .map(_.map(_.signed.value.ordinal).getOrElse(SnapshotOrdinal.MinValue))
            .map(_.next)

          _record = TaskRecord(
            modelID = update.modelID,
            creationOrdinal = currentOrdinal,
            lastUpdatedOrdinal = currentOrdinal,
            model = update.model,
            typeOfmodel = update.typeOfmodel,
            primaryUseCase = update.primaryUseCase,
            keyFeatures = update.keyFeatures,
            exampleOfApplication  = update.exampleOfApplication,
            timeStampCreation = update.timeStampCreation,
            timeStampCompletion = update.timeStampCompletion,
            timeStampUpdate = update.timeStampUpdate,
            ageOfModel = update.ageOfModel,
            versionControl = update.versionControl,
            watermarked = update.watermarked
          )

          onchain = inState.onChain
            .focus(_.activeTasks)
            .modify(_.updated(update.modelID, _record))

          calculated = inState.calculated

        } yield DataState(onchain, calculated)


    }
}
