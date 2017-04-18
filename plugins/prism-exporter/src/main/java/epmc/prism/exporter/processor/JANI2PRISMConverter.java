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

package epmc.prism.exporter.processor;

import epmc.error.EPMCException;
import epmc.jani.model.ModelJANI;
import epmc.modelchecker.Properties;

public final class JANI2PRISMConverter {

	private final ModelJANI jani;
	
	public JANI2PRISMConverter(ModelJANI jani) {
		assert jani != null;
		JANIComponentRegistrar.reset();
		
		this.jani = jani;
	}
	
	public StringBuilder convertModel() throws EPMCException {
		assert jani != null;
		
		JANI2PRISMProcessorStrict processor; 
		
		processor = ProcessorRegistrar.getProcessor(jani);
		return processor.toPRISM();
	}

	public StringBuilder convertProperties() throws EPMCException {
		assert jani != null;
		
		JANI2PRISMProcessorStrict processor; 
		
		Properties properties = jani.getPropertyList();
		processor = ProcessorRegistrar.getProcessor(properties);
		return processor.toPRISM();
	}
}
