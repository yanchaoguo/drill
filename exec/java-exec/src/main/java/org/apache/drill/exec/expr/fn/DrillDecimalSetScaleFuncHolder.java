/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.drill.exec.expr.fn;

import java.util.List;
import java.util.Map;

import org.apache.drill.common.expression.LogicalExpression;
import org.apache.drill.common.expression.ValueExpressions;
import org.apache.drill.common.types.TypeProtos;
import org.apache.drill.common.types.TypeProtos.MajorType;
import org.apache.drill.exec.expr.DrillSimpleFunc;
import org.apache.drill.exec.expr.annotations.FunctionTemplate;
import org.apache.drill.exec.expr.annotations.FunctionTemplate.FunctionScope;
import org.apache.drill.exec.expr.annotations.FunctionTemplate.NullHandling;

public class DrillDecimalSetScaleFuncHolder extends DrillSimpleFuncHolder{


  public DrillDecimalSetScaleFuncHolder(FunctionScope scope, NullHandling nullHandling, boolean isBinaryCommutative, boolean isRandom,
                                        String[] registeredNames, ValueReference[] parameters, ValueReference returnValue, WorkspaceReference[] workspaceVars,
                                        Map<String, String> methods, List<String> imports, FunctionTemplate.FunctionCostCategory costCategory, Class<? extends DrillSimpleFunc> drillFuncClass) {
    super(scope, nullHandling, isBinaryCommutative, isRandom, registeredNames, parameters, returnValue, workspaceVars, methods, imports, costCategory, drillFuncClass);
  }

  @Override
  public MajorType getReturnType(List<LogicalExpression> args) {

    TypeProtos.DataMode mode = returnValue.type.getMode();
    int scale = 0;
    int precision = 0;
    int i = 0;

    if (nullHandling == NullHandling.NULL_IF_NULL) {
      // if any one of the input types is nullable, then return nullable return type
      for (LogicalExpression e : args) {

        precision = Math.max(precision, e.getMajorType().getPrecision());
        if (e.getMajorType().getMode() == TypeProtos.DataMode.OPTIONAL) {
          mode = TypeProtos.DataMode.OPTIONAL;
        }
      }

      /* Used by functions like round, truncate which specify the scale for
       * the output as the second argument
       */
      assert (args.size() == 2) && (args.get(1) instanceof ValueExpressions.IntExpression);

      // Get the scale from the second argument which should be a constant
      scale = ((ValueExpressions.IntExpression) args.get(1)).getInt();
    }

    return (TypeProtos.MajorType.newBuilder().setMinorType(returnValue.type.getMinorType()).setScale(scale).setPrecision(precision).setMode(mode).build());
  }
}
