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

package epmc.jani.model;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import epmc.jani.exporter.processor.JANIProcessor;
import epmc.jani.exporter.processor.ProcessorRegistrar;

public class AutomatonProcessor implements JANIProcessor {
    /** String identifying the name of an automaton. */
    private final static String NAME = "name";
    /** String identifying the variables of an automaton. */
    private final static String VARIABLES = "variables";
    /** String identifying the locations of an automaton. */
    private final static String LOCATIONS = "locations";
    /** String identifying the initial location of an automaton. */
    private final static String INITIAL_LOCATIONS = "initial-locations";
    /** String identifying the edges of an automaton. */
    private final static String EDGES = "edges";
    /** String identifying comment for this automaton. */
    private final static String COMMENT = "comment";
    /** String identifying initial variable values of this automaton. */
    private final static String RESTRICT_INITIAL = "restrict-initial";

    private Automaton automaton = null;

    @Override
    public JANIProcessor setElement(Object component) {
        assert component != null;
        assert component instanceof Automaton;

        automaton = (Automaton) component;
        return this;
    }

    @Override
    public JsonValue toJSON() {
        assert automaton != null;

        JsonObjectBuilder builder = Json.createObjectBuilder();
        
        builder.add(NAME, automaton.getName());
        
        Variables variables = automaton.getVariables();
        if (variables != null) {
            builder.add(VARIABLES, ProcessorRegistrar.getProcessor(variables)
                    .toJSON());
        }

        InitialStates initialStates = automaton.getInitialStates();
        if (initialStates != null) {
            builder.add(RESTRICT_INITIAL, ProcessorRegistrar.getProcessor(initialStates)
                    .toJSON());
        }
        
        builder.add(LOCATIONS, ProcessorRegistrar.getProcessor(automaton.getLocations())
                .toJSON());
        
        JsonArrayBuilder initialLocations = Json.createArrayBuilder();
        for (Location initialLocation : automaton.getInitialLocations()) {
            initialLocations.add(initialLocation.getName());
        }
        builder.add(INITIAL_LOCATIONS, initialLocations);
        
        builder.add(EDGES, ProcessorRegistrar.getProcessor(automaton.getEdges())
                .toJSON());
        
        String comment = automaton.getComment();
        if (comment != null) {
            builder.add(COMMENT, comment);
        }
        
        return builder.build();
    }	
}
