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

package epmc.dd;

import epmc.error.EPMCException;
import epmc.plugin.AfterCommandExecution;
import epmc.value.ContextValue;

public final class AfterCommandExecutionDD implements AfterCommandExecution {
	public final static String IDENTIFIER = "after-command-execution-dd";
	
	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}

	@Override
	public void process() throws EPMCException {
		ContextDD.close(ContextValue.get());
	}

}
