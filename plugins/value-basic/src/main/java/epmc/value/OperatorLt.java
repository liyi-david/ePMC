/****************************************************************************

    ePMC - an extensible probabilistic model checker
    Copyright (C) 2017

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

*****************************************************************************/

package epmc.value;

import epmc.error.EPMCException;
import epmc.value.ContextValue;
import epmc.value.Operator;
import epmc.value.Type;
import epmc.value.TypeUnknown;
import epmc.value.Value;

public final class OperatorLt implements Operator {
    private ContextValue context;
    /** Less than, a < b, binary operator. */
    public final static String IDENTIFIER = "<";

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public void setContext(ContextValue context) {
        this.context = context;
    }

    @Override
    public ContextValue getContext() {
        return context;
    }

    @Override
    public void apply(Value result, Value... operands) throws EPMCException {
    	ValueBoolean.asBoolean(result).set(ValueAlgebra.asAlgebra(operands[0]).isLt(operands[1]));
    }

    @Override
    public Type resultType(Type... types) {
        for (Type type : types) {
            if (!TypeUnknown.isUnknown(type) && !TypeAlgebra.isAlgebra(type)) {
                return null;
            }
        }
        Type result = TypeBoolean.get(getContext());
        return result;
    }

    @Override
    public String toString() {
        return IDENTIFIER;
    }
}
