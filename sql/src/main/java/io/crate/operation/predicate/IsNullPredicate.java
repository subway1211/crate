/*
 * Licensed to CRATE Technology GmbH ("Crate") under one or more contributor
 * license agreements.  See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.  Crate licenses
 * this file to you under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.  You may
 * obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * However, if you have executed another commercial license agreement
 * with Crate these terms will supersede the license and you may use the
 * software solely pursuant to the terms of the relevant commercial agreement.
 */

package io.crate.operation.predicate;

import com.google.common.base.Preconditions;
import io.crate.analyze.symbol.Function;
import io.crate.analyze.symbol.Literal;
import io.crate.analyze.symbol.Symbol;
import io.crate.analyze.symbol.format.FunctionFormatSpec;
import io.crate.metadata.*;
import io.crate.operation.Input;
import io.crate.types.DataType;
import io.crate.types.DataTypes;

import java.util.List;


public class IsNullPredicate<T> extends Scalar<Boolean, T> implements FunctionFormatSpec {

    public static final String NAME = "op_isnull";
    private final FunctionInfo info;

    public static void register(PredicateModule module) {
        module.register(NAME, new Resolver());
    }

    public static FunctionInfo generateInfo(List<DataType> types) {
        return new FunctionInfo(new FunctionIdent(NAME, types), DataTypes.BOOLEAN, FunctionInfo.Type.PREDICATE);
    }

    IsNullPredicate(FunctionInfo info) {
        this.info = info;
    }


    @Override
    public FunctionInfo info() {
        return info;
    }

    @Override
    public Symbol normalizeSymbol(Function symbol, TransactionContext transactionContext) {
        assert symbol != null : "function must not be null";
        assert symbol.arguments().size() == 1 : "function's number of arguments must be 1";

        Symbol arg = symbol.arguments().get(0);
        if (arg.equals(Literal.NULL) || arg.valueType().equals(DataTypes.UNDEFINED)) {
            return Literal.of(true);
        } else if (arg.symbolType().isValueSymbol()) {
            return Literal.of(((Input) arg).value() == null);
        }
        return symbol;
    }

    @Override
    public Boolean evaluate(Input[] args) {
        assert args.length == 1 : "number of args must be 1";
        return args[0] == null || args[0].value() == null;
    }

    @Override
    public String beforeArgs(Function function) {
        return "";
    }

    @Override
    public String afterArgs(Function function) {
        return " IS NULL";
    }

    @Override
    public boolean formatArgs(Function function) {
        return true;
    }

    private static class Resolver implements FunctionResolver {

        @Override
        public FunctionImplementation getForTypes(List<DataType> dataTypes) throws IllegalArgumentException {
            Preconditions.checkArgument(
                dataTypes.size() == 1, "the is null predicate takes only 1 argument");

            return new IsNullPredicate<>(generateInfo(dataTypes));
        }

        @Override
        public List<Signature> signatures() {
            return Signature.SIGNATURES_SINGLE_ANY;
        }
    }
}
