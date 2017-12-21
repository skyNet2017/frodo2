package com.fernandocejas.frodo2.logger.flowable;

import com.fernandocejas.frodo2.logger.joinpoint.FrodoProceedingJoinPoint;
import com.fernandocejas.frodo2.logger.logging.MessageBuilder;
import com.fernandocejas.frodo2.logger.logging.MessageManager;
import com.fernandocejas.frodo2.logging.Logger;
import io.reactivex.Flowable;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

/**
 * Copyright (C) 2017 android10.org Open Source Project
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class FlowableWeaver {

  public static final String POINTCUT =
      "execution(@com.fernandocejas.frodo2.annotation.RxLogFlowable * *(..)) && if()";
  public static final String ADVICE = "methodAnnotatedWithRxLogFlowable(joinPoint)";

  public static boolean methodAnnotatedWithRxLogFlowable(ProceedingJoinPoint joinPoint) {
    return ((MethodSignature) joinPoint.getSignature()).getReturnType() == Flowable.class;
  }

  public Object weaveAroundJoinPoint(ProceedingJoinPoint joinPoint, Logger logger)
      throws Throwable {
    final FrodoProceedingJoinPoint proceedingJoinPoint = new FrodoProceedingJoinPoint(joinPoint);
    final MessageManager messageManager = new MessageManager(new MessageBuilder(), logger);
    return new FrodoForFlowable(proceedingJoinPoint, messageManager).flowable();
  }
}
