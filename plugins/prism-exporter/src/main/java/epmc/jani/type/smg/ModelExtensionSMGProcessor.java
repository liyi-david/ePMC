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

package epmc.jani.type.smg;

import java.util.LinkedList;
import java.util.List;

import epmc.prism.exporter.messages.ExtendedFeaturesPRISMExporter;
import epmc.prism.exporter.processor.JANI2PRISMProcessorExtended;
import epmc.prism.exporter.processor.ProcessorRegistrar;

public final class ModelExtensionSMGProcessor implements JANI2PRISMProcessorExtended {
	
	private ModelExtensionSMG smg;

	@Override
	public JANI2PRISMProcessorExtended setElement(Object obj) {
		assert obj instanceof ModelExtensionSMG;
		
		smg = (ModelExtensionSMG) obj;
		return this;
	}

	@Override
	public String toPRISM() {
		assert smg != null;
		
		return new StringBuilder().append(ModelExtensionSMG.IDENTIFIER)
								  .append("\n")
								  .append(ProcessorRegistrar.getProcessor(smg.getPlayers()).toPRISM())
								  .toString();
	}
	
	
	@Override
	public List<String> getUnsupportedFeature() {
		List<String> ll = new LinkedList<>();
		ll.add(ExtendedFeaturesPRISMExporter.PRISM_EXPORTER_EXTENDED_FEATURE_SEMANTIC_TYPE_SMG);
		return ll;
	}
	
	@Override
	public void validateTransientVariables() {
		assert smg != null;
		
		ProcessorRegistrar.getProcessor(smg.getPlayers()).validateTransientVariables();
	}

	@Override
	public boolean usesTransientVariables() {
		assert smg != null;
		
		return ProcessorRegistrar.getProcessor(smg.getPlayers()).usesTransientVariables();
	}	
}
