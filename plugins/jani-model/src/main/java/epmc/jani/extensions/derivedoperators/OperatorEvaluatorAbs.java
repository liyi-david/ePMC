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

package epmc.jani.extensions.derivedoperators;

import epmc.error.EPMCException;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;
import epmc.value.TypeNumber;
import epmc.value.Value;
import epmc.value.ValueNumber;

/**
 * Operator to compute absolute value of a value.
 * 
 * @author Ernst Moritz Hahn
 */
public final class OperatorEvaluatorAbs implements OperatorEvaluator {

	@Override
	public boolean canApply(String operator, Type... types) {
		assert operator != null;
		assert types != null;
		for (Type type : types) {
			assert type != null;
		}
		if (!operator.equals(OperatorAbs.IDENTIFIER)) {
			return false;
		}
		if (types.length != 1) {
			return false;
		}
		if (!TypeNumber.isNumber(types[0])) {
			return false;
		}
		return true;
	}

	@Override
	public Type resultType(String operator, Type... types) {
		assert operator != null;
		assert types != null;
		assert types.length >= 1;
		assert types[0] != null;
		return types[0];
	}

	@Override
	public void apply(Value result, String operator, Value... operands) throws EPMCException {
		assert result != null;
		assert operator != null;
		assert operands != null;
		assert operands.length >= 1;
		assert operands[0] != null;
		ValueNumber.asNumber(result).abs(operands[0]);
	}
}