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

import epmc.util.BitStream;
import epmc.value.Value;

public final class ValueTernary implements ValueEnumerable, ValueBitStoreable, ValueSetString {
    public static boolean isTernary(Value value) {
        return value instanceof ValueTernary;
    }

    public static ValueTernary asTernary(Value value) {
        if (isTernary(value)) {
            return (ValueTernary) value;
        } else {
            return null;
        }
    }

    public static boolean isUnknown(Value value) {
        ValueTernary valueTernary = asTernary(value);
        if (valueTernary == null) {
            return false;
        }
        if (valueTernary.getTernary().isUnknown()) {
            return true;
        }
        return false;
    }

    private Ternary value;
    private final TypeTernary type;
    private boolean immutable;

    ValueTernary(TypeTernary type, Ternary value) {
        assert type != null;
        this.type = type;
        this.value = value;
    }

    ValueTernary(TypeTernary type) {
        this(type, Ternary.UNKNOWN);
    }

    public boolean getBoolean() {
        return value.getBoolean();
    }

    public void set(Ternary value) {
        assert !isImmutable();
        this.value = value;

    }

    public void set(boolean value) {
        assert !isImmutable();
        this.value = value ? Ternary.TRUE : Ternary.FALSE;

    }

    @Override
    public TypeTernary getType() {
        return type;
    }

    @Override
    public ValueTernary clone() {
        return new ValueTernary(getType(), value);
    }

    @Override
    public boolean equals(Object obj) {
        assert obj != null;
        if (!(obj instanceof ValueTernary)) {
            return false;
        }
        ValueTernary other = (ValueTernary) obj;
        return this.value == other.value;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash = getClass().hashCode() + (hash << 6) + (hash << 16) - hash;
        hash = value.hashCode() + (hash << 6) + (hash << 16) - hash;
        return hash;
    }

    @Override
    public String toString() {
        return value.toString();
    }

    @Override
    public void set(Value from) {
        assert !isImmutable();
        assert ValueBoolean.isBoolean(from) || ValueTernary.isTernary(from);
        if (ValueBoolean.isBoolean(from)) {
            set(ValueBoolean.asBoolean(from).getBoolean());
        } else if (ValueTernary.isTernary(from)) {
            ValueTernary fromTernary = (ValueTernary) from;
            set(fromTernary.getTernary());
        }

    }

    @Override
    public boolean isEq(Value operand) {
        assert operand != null;
        assert ValueBoolean.isBoolean(operand) || ValueTernary.isTernary(operand);
        Ternary operandTernary = UtilTernary.getTernary(operand);
        return operandTernary == value;
    }

    @Override
    public void read(BitStream reader) {
        assert !isImmutable();
        assert reader != null;
        int ord = 0;
        if (reader.read()) {
            ord |= 1;
        }
        if (reader.read()) {
            ord |= 2;
        }
        set(Ternary.values()[ord]);
    }

    @Override
    public void write(BitStream writer) {
        assert writer != null;
        int ord = value.ordinal();
        writer.write((ord & 1) > 0);
        writer.write((ord & 2) > 0);
    }

    public Ternary getTernary() {
        return value;
    }

    @Override
    public int compareTo(Value other) {
        assert other != null;
        assert ValueBoolean.isBoolean(other) || ValueTernary.isTernary(other);
        Ternary otherTernary = UtilTernary.getTernary(other);
        return value.compareTo(otherTernary);
    }

    @Override
    public void set(String string) {
        assert string != null;
        string = string.toLowerCase().trim();
        Ternary value;
        switch (string) {
        case "true":
            value = Ternary.TRUE;
            break;
        case "false":
            value = Ternary.FALSE;
            break;
        case "unknown": case "?":
            value = Ternary.UNKNOWN;
            break;
        default:
            assert false;
            value = null;
            break;
        }
        this.value = value;

    }

    @Override
    public int getValueNumber() {
        switch (value) {
        case FALSE:
            return 0;
        case UNKNOWN:
            return 1;
        case TRUE:
            return 2;
        default:
            assert false;
            return -1;
        }
    }

    @Override
    public void setImmutable() {
        this.immutable = true;
    }

    @Override
    public boolean isImmutable() {
        return immutable;
    }

    @Override
    public double distance(Value other) {
        ValueTernary otherTernary = asTernary(other);
        return value.equals(otherTernary.value) ? 0.0 : 1.0;
    }

    @Override
    public void setValueNumber(int number) {
        assert getType().canImport(getType()) : value;
        assert number >= 0 : number;
        assert number < TypeTernary.NUM_VALUES : number;
        switch (number) {
        case TypeTernary.FALSE_NUMBER:
            value = Ternary.FALSE;
            break;
        case TypeTernary.UNKNOWN_NUMBER:
            value = Ternary.UNKNOWN;
            break;
        case TypeTernary.TRUE_NUMBER:
            value = Ternary.TRUE;
            break;
        default:
            assert false;
        }
    }
}
