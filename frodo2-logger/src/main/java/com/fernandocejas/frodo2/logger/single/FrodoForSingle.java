package com.fernandocejas.frodo2.logger.single;

import com.fernandocejas.frodo2.logger.joinpoint.FrodoProceedingJoinPoint;
import com.fernandocejas.frodo2.logger.joinpoint.RxComponent;
import com.fernandocejas.frodo2.logger.joinpoint.RxComponentInfo;
import com.fernandocejas.frodo2.logger.logging.MessageManager;
import com.fernandocejas.frodo2.logger.logging.StopWatch;

import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.BiConsumer;
import io.reactivex.functions.Consumer;

@SuppressWarnings({"unchecked", "Convert2Lambda"})
class FrodoForSingle {

  private final FrodoProceedingJoinPoint joinPoint;
  private final MessageManager messageManager;
  private final RxComponentInfo rxComponentInfo;

  FrodoForSingle(FrodoProceedingJoinPoint joinPoint, MessageManager messageManager) {
    this.joinPoint = joinPoint;
    this.messageManager = messageManager;
    this.rxComponentInfo = new RxComponentInfo(RxComponent.SINGLE, joinPoint);
  }

  Single single() throws Throwable {
    messageManager.printRxComponentInfo(rxComponentInfo);
    final Class singleType = joinPoint.getGenericReturnTypes().get(0);
    return loggableSingle(singleType);
  }

  private <T> Single<T> loggableSingle(T type) throws Throwable {
    final StopWatch stopWatch = new StopWatch();
    return ((Single<T>) joinPoint.proceed())
        .doOnSubscribe(new Consumer<Disposable>() {
          @Override
          public void accept(Disposable disposable) throws Exception {
            stopWatch.start();
            messageManager.printOnSubscribe(rxComponentInfo);
          }
        })
        .doOnEvent(new BiConsumer<T, Throwable>() {
          @Override
          public void accept(T value, Throwable throwable) throws Exception {
            if (rxComponentInfo.observeOnThread() == null) {
              rxComponentInfo.setObserveOnThread(Thread.currentThread().getName());
            }

            if (value != null) { // success
              messageManager.printOnSuccessWithValue(rxComponentInfo, value);
              rxComponentInfo.setTotalEmittedItems(1);
            } else { // error
              messageManager.printOnError(rxComponentInfo, throwable);
            }
          }
        })
        .doOnDispose(new Action() {
          @Override
          public void run() throws Exception {
            messageManager.printOnDispose(rxComponentInfo);
          }
        })
        .doFinally(new Action() {
          @Override
          public void run() throws Exception {
            stopWatch.stop();
            rxComponentInfo.setTotalExecutionTime(stopWatch.getTotalTimeMillis());
            messageManager.printItemTimeInfo(rxComponentInfo);
            messageManager.printThreadInfo(rxComponentInfo);
          }
        });
  }
}
