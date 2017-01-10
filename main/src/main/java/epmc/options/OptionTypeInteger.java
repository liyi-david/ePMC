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

package epmc.options;

import static epmc.error.UtilError.ensure;
import static epmc.error.UtilError.fail;

import epmc.error.EPMCException;

/**
 * Option type for integer options.
 * The values will be read by {@link Integer#parseInt(String)}. Strings which
 * cannot be parsed correctly this way will result in an
 * {@link EPMCException} being thrown.
 * 
 * @author Ernst Moritz Hahn
 */
public final class OptionTypeInteger implements OptionType {
    /** String returned by {@link #getInfo()} method. */
    private final static String INFO = "<integer>";
    /** Integer option type. */
    final static OptionTypeInteger INSTANCE = new OptionTypeInteger();
    
    /**
     * Private constructor.
     * We want the option type to be obtained using
     * {@link OptionTypeInteger#getInstance()} rather than by directly calling
     * the constructor.
     */
    private OptionTypeInteger() {   
    }
    
    @Override
    public Object parse(String value, Object prevValue) throws EPMCException {
        assert value != null;
        ensure(prevValue == null, ProblemsOptions.OPTIONS_OPT_CMD_LINE_SET_MULTIPLE);
        value = value.trim();
        try {
            Integer.parseInt(value);
            return value;
        } catch (NumberFormatException e) {
            fail(ProblemsOptions.OPTIONS_INV_PRG_OPT_VALUE, e, value);
            return null;
        }
    }
    
    @Override
    public String getInfo() {
        return INFO;
    }
    
    @Override
    public String toString() {
        return getInfo();
    }

    /**
     * Get integer option type.
     * 
     * @return integer option type
     */
    public static OptionTypeInteger getInstance() {
        return INSTANCE;
    }
}
