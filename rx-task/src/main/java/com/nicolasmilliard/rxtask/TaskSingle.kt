package com.nicolasmilliard.rxtask

import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import io.reactivex.Single
import io.reactivex.SingleObserver
import io.reactivex.disposables.Disposable
import io.reactivex.exceptions.CompositeException
import io.reactivex.exceptions.Exceptions
import io.reactivex.plugins.RxJavaPlugins

class TaskSingle<T>(private val task: Task<T>) : Single<T>() {
    override fun subscribeActual(observer: SingleObserver<in T>) {
        val callback = TaskCallback<T>(observer)
        observer.onSubscribe(callback)
        task.addOnCompleteListener(callback)
    }

    internal class TaskCallback<T>(private val observer: SingleObserver<in T>) : Disposable, OnCompleteListener<T> {
        private var disposed = false

        override fun onComplete(task: Task<T>) {
            if (disposed) return
            if (task.isSuccessful) {
                observer.onSuccess(task.result)
            } else {
                try {
                    observer.onError(task.exception!!)
                } catch (inner: Throwable) {
                    Exceptions.throwIfFatal(inner)
                    RxJavaPlugins.onError(CompositeException(task.exception, inner))
                }
            }
        }

        override fun isDisposed(): Boolean = disposed

        override fun dispose() {
            disposed = true
        }

    }
}