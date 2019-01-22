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

package epmc.automaton.hoa;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import epmc.expression.Expression;
import epmc.expression.standard.ExpressionIdentifierStandard;
import epmc.util.BitSet;
import epmc.util.BitSetUnboundedLongArray;

public final class HanoiHeader {
    private String name;
    private String toolName;
    private String toolVersion;
    private final Map<String,Expression> ap2expr;
    private int numStates;
    private final BitSet startStates = new BitSetUnboundedLongArray();
    private int numAPs;
    private final List<Expression> aps = new ArrayList<>();
    private int numAcc;
    private final LinkedHashSet<String> properties = new LinkedHashSet<>();
    private final Set<String> propertiesReadonly = Collections.unmodifiableSet(properties);
    private Acceptance acceptance;
    private AcceptanceName acceptanceName;

    HanoiHeader(Map<String,Expression> ap2expr) {
        this.ap2expr = ap2expr;
    }

    void setName(String name) {
        this.name = name.substring(1, name.length() - 1);
    }
    
    public String getName() {
        return name;
    }
    
    void setToolName(String toolName) {
        this.toolName = toolName.substring(1, toolName.length() - 1);
    }

    public String getToolName() {
        return toolName;
    }

    void setToolVersion(String toolVersion) {
        this.toolVersion = toolVersion.substring(1, toolVersion.length() - 1);
    }

    public String getToolVersion() {
        return toolVersion;
    }
    
    void addProperty(String property) {
        properties.add(property);
    }

    void setNumAccSets(int numAccSets) {
        this.numAcc = numAccSets;
    }
    
    public int getNumAccSets() {
        return numAcc;
    }
    
    void setAcceptance(Acceptance acceptance) {
        this.acceptance = acceptance;
    }
    
    public Acceptance getAcceptance() {
        return acceptance;
    }
    
    void setAcceptanceName(AcceptanceName acceptanceName) {
        this.acceptanceName = acceptanceName;
    }
    
    public AcceptanceName getAcceptanceName() {
        return acceptanceName;
    }
    
    public Set<String> getProperties() {
        return propertiesReadonly;
    }
    
    void setNumStates(int numStates) {
        this.numStates = numStates;
    }

    int getNumStates() {
        return numStates;
    }

    void setStartState(int startState) {
        startStates.set(startState);
    }

    BitSet getStartStates() {
        return startStates;
    }

    void setNumAPs(int numAPs) {
        this.numAPs = numAPs;
    }
    
    public int getNumAPs() {
        return numAPs;
    }
    
    void addAP(String name) {
        assert name != null;
        name = name.substring(1, name.length() - 1);
        assert ap2expr == null || ap2expr.containsKey(name);
        if (ap2expr != null) {
            aps.add(ap2expr.get(name));
        } else {
            aps.add(new ExpressionIdentifierStandard.Builder()
                    .setName(name).build());
        }
    }

    Expression numberToIdentifier(int number) {
        assert number >= 0;
        assert number < aps.size();
        return aps.get(number);
    }

    void setNumAcc(int numAcc) {
        this.numAcc = numAcc;
    }

    public int getNumAcc() {
        return numAcc;
    }
    
    public List<Expression> getAps() {
        return aps;
    }
}
