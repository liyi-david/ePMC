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

/* Generated By:JavaCC: Do not edit this line. SpotParser.java */
package epmc.automaton;

import epmc.util.BitSet;
import epmc.util.BitSetUnboundedLongArray;
import java.util.Map;
import epmc.automaton.ProblemsAutomaton;
import static epmc.error.UtilError.ensure;
import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.expression.standard.ExpressionOperator;
import epmc.expression.standard.ExpressionLiteral;
import epmc.value.ContextValue;
import epmc.value.OperatorOr;
import epmc.value.OperatorAnd;
import epmc.value.OperatorNot;
import epmc.graph.explicit.GraphExplicitWrapper;

public class SpotParser implements SpotParserConstants {
  private ContextValue context;

  public GraphExplicitWrapper parseAutomaton(ContextValue context, Map<String,Expression> ap2expr)
      throws EPMCException {
    assert context != null;
    assert ap2expr != null;
    this.context = context;
    try {
      return Automaton(context, ap2expr);
    } catch (ParseException e) {
      ensure(false, ProblemsAutomaton.LTL2BA_SPOT_PROBLEM_PARSE, e);
      return null;
    }
  }

  private Expression and(Expression a, Expression b) {
      return new ExpressionOperator.Builder()
        .setOperator(context.getOperator(OperatorAnd.IDENTIFIER))
        .setOperands(a, b)
        .build();
  }

    private Expression or(Expression a, Expression b) {
        return new ExpressionOperator.Builder()
            .setOperator(context.getOperator(OperatorOr.IDENTIFIER))
            .setOperands(a, b)
            .build();
    }

    private Expression not(Expression expression) {
        return new ExpressionOperator.Builder()
                .setOperator(context.getOperator(OperatorNot.IDENTIFIER))
                .setOperands(expression)
                .build();
    }

  final public GraphExplicitWrapper Automaton(ContextValue context, Map<String,Expression> ap2expr) throws ParseException, EPMCException {
  HanoiHeader header;
  GraphExplicitWrapper graph;
    header = Header(context, ap2expr);
    graph = Body(header);
    {if (true) return graph;}
    throw new Error("Missing return statement in function");
  }

  final public HanoiHeader Header(ContextValue context, Map<String,Expression> ap2expr) throws ParseException {
  HanoiHeader header = new HanoiHeader(context, ap2expr);
  int numStates;
  int startState;
  Token ap;
  int numAccs;
    label_1:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case HOA:
      case NAME:
      case STATES:
      case START:
      case AP:
      case ACC_NAME:
      case ACCEPTANCE:
      case PROPERTIES:
        ;
        break;
      default:
        jj_la1[0] = jj_gen;
        break label_1;
      }
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case HOA:
        jj_consume_token(HOA);
        jj_consume_token(COLON);
        jj_consume_token(V1);
        break;
      case NAME:
        jj_consume_token(NAME);
        jj_consume_token(COLON);
        Identifiers();
        break;
      case STATES:
        jj_consume_token(STATES);
        jj_consume_token(COLON);
        numStates = parseInt();
                                            header.setNumStates(numStates);
        break;
      case START:
        jj_consume_token(START);
        jj_consume_token(COLON);
        startState = parseInt();
                                            header.setStartState(startState);
        break;
      case AP:
        jj_consume_token(AP);
        jj_consume_token(COLON);
        parseInt();
        label_2:
        while (true) {
          switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
          case QUOTE:
            ;
            break;
          default:
            jj_la1[1] = jj_gen;
            break label_2;
          }
          jj_consume_token(QUOTE);
          ap = jj_consume_token(IDENTIFIER);
          jj_consume_token(QUOTE);
                                                                header.addAP(ap.toString());
        }
        break;
      case ACC_NAME:
        jj_consume_token(ACC_NAME);
        jj_consume_token(COLON);
        switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
        case BUCHI:
          jj_consume_token(BUCHI);
               header.setNumAcc(1);
          break;
        case GENERALIZED_BUCHI:
          jj_consume_token(GENERALIZED_BUCHI);
          numAccs = parseInt();
                                                header.setNumAcc(numAccs);
          break;
        case ALL:
          jj_consume_token(ALL);
            header.setNumAcc(0);
          break;
        default:
          jj_la1[2] = jj_gen;
          jj_consume_token(-1);
          throw new ParseException();
        }
        break;
      case ACCEPTANCE:
        jj_consume_token(ACCEPTANCE);
        jj_consume_token(COLON);
        parseInt();
        switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
        case INF:
          jj_consume_token(INF);
          jj_consume_token(LPARENTH);
          parseInt();
          jj_consume_token(RPARENTH);
          label_3:
          while (true) {
            switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
            case AND:
              ;
              break;
            default:
              jj_la1[3] = jj_gen;
              break label_3;
            }
            jj_consume_token(AND);
            jj_consume_token(INF);
            jj_consume_token(LPARENTH);
            parseInt();
            jj_consume_token(RPARENTH);
          }
          break;
        case T:
          jj_consume_token(T);
          break;
        default:
          jj_la1[4] = jj_gen;
          jj_consume_token(-1);
          throw new ParseException();
        }
        break;
      case PROPERTIES:
        jj_consume_token(PROPERTIES);
        jj_consume_token(COLON);
        label_4:
        while (true) {
          switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
          case TRANS_LABELS:
          case EXPLICIT_LABELS:
          case TRANS_ACC:
          case STATE_ACC:
          case STUTTER_INVARIANT:
          case DETERMINISTIC:
          case INHERENTLY_WEAK:
          case COMPLETE:
          case TERMINAL:
            ;
            break;
          default:
            jj_la1[5] = jj_gen;
            break label_4;
          }
          switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
          case TRANS_LABELS:
            jj_consume_token(TRANS_LABELS);
            break;
          case EXPLICIT_LABELS:
            jj_consume_token(EXPLICIT_LABELS);
            break;
          case TRANS_ACC:
            jj_consume_token(TRANS_ACC);
            break;
          case STUTTER_INVARIANT:
            jj_consume_token(STUTTER_INVARIANT);
            break;
          case DETERMINISTIC:
            jj_consume_token(DETERMINISTIC);
            break;
          case STATE_ACC:
            jj_consume_token(STATE_ACC);
            break;
          case INHERENTLY_WEAK:
            jj_consume_token(INHERENTLY_WEAK);
            break;
          case COMPLETE:
            jj_consume_token(COMPLETE);
            break;
          case TERMINAL:
            jj_consume_token(TERMINAL);
            break;
          default:
            jj_la1[6] = jj_gen;
            jj_consume_token(-1);
            throw new ParseException();
          }
        }
        break;
      default:
        jj_la1[7] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
    }
    {if (true) return header;}
    throw new Error("Missing return statement in function");
  }

  final public GraphExplicitWrapper Body(HanoiHeader header) throws ParseException, EPMCException {
  GraphPreparator graph = new GraphPreparator(header);
    jj_consume_token(BODY);
    label_5:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case STATE:
        ;
        break;
      default:
        jj_la1[8] = jj_gen;
        break label_5;
      }
      State(graph);
    }
    jj_consume_token(END);
    {if (true) return graph.toGraph();}
    throw new Error("Missing return statement in function");
  }

  final public void State(GraphPreparator graph) throws ParseException, EPMCException {
  int from;
  Expression guard;
  int to;
  BitSet acc;
  int accState;
    jj_consume_token(STATE);
    jj_consume_token(COLON);
    from = parseInt();
    label_6:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case LBRACK:
        ;
        break;
      default:
        jj_la1[9] = jj_gen;
        break label_6;
      }
      jj_consume_token(LBRACK);
      guard = Guard(graph.getHeader());
      jj_consume_token(RBRACK);
      to = parseInt();
          acc = new BitSetUnboundedLongArray();
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case LCURLY:
        jj_consume_token(LCURLY);
        label_7:
        while (true) {
          switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
          case NUM_INT:
            ;
            break;
          default:
            jj_la1[10] = jj_gen;
            break label_7;
          }
          accState = parseInt();
                                           acc.set(accState);
        }
        jj_consume_token(RCURLY);
        break;
      default:
        jj_la1[11] = jj_gen;
        ;
      }
          graph.addTransition(from, to, guard, acc);
    }
  }

  final public String Identifier() throws ParseException {
  Token literal;
  String result;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case IDENTIFIER:
      literal = jj_consume_token(IDENTIFIER);
                             result = literal.toString();
      break;
    case QUOTE:
      jj_consume_token(QUOTE);
      literal = jj_consume_token(IDENTIFIER);
                                      result = literal.toString();
      jj_consume_token(QUOTE);
      break;
    default:
      jj_la1[12] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    {if (true) return result;}
    throw new Error("Missing return statement in function");
  }

  final public String Identifiers() throws ParseException {
  Token literal;
  String result = "";
    jj_consume_token(QUOTE);
    label_8:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case NOT:
      case AND:
      case OR:
      case LPARENTH:
      case RPARENTH:
      case IDENTIFIER:
        ;
        break;
      default:
        jj_la1[13] = jj_gen;
        break label_8;
      }
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case IDENTIFIER:
        literal = jj_consume_token(IDENTIFIER);
                               result += literal.toString();
        break;
      case AND:
        literal = jj_consume_token(AND);
                          result += literal.toString();
        break;
      case OR:
        literal = jj_consume_token(OR);
                         result += literal.toString();
        break;
      case NOT:
        literal = jj_consume_token(NOT);
                          result += literal.toString();
        break;
      case LPARENTH:
        literal = jj_consume_token(LPARENTH);
                               result += literal.toString();
        break;
      case RPARENTH:
        literal = jj_consume_token(RPARENTH);
                               result += literal.toString();
        break;
      default:
        jj_la1[14] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
    }
    jj_consume_token(QUOTE);
    {if (true) return result;}
    throw new Error("Missing return statement in function");
  }

  final public Expression Guard(HanoiHeader header) throws ParseException {
  Expression expr;
    expr = ExpressionOr(header);
    {if (true) return expr;}
    throw new Error("Missing return statement in function");
  }

  final public Expression ExpressionOr(HanoiHeader header) throws ParseException {
  Expression p;
  Expression nextProp;
    p = ExpressionAnd(header);
    label_9:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case OR:
        ;
        break;
      default:
        jj_la1[15] = jj_gen;
        break label_9;
      }
      jj_consume_token(OR);
      nextProp = ExpressionAnd(header);
      p = or(p, nextProp);
    }
    {if (true) return p;}
    throw new Error("Missing return statement in function");
  }

  final public Expression ExpressionAnd(HanoiHeader header) throws ParseException {
  Expression p;
  Expression nextProp;
    p = ExpressionNot(header);
    label_10:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case AND:
        ;
        break;
      default:
        jj_la1[16] = jj_gen;
        break label_10;
      }
      jj_consume_token(AND);
      nextProp = ExpressionNot(header);
      p = and(p, nextProp);
    }
    {if (true) return p;}
    throw new Error("Missing return statement in function");
  }

  final public Expression ExpressionNot(HanoiHeader header) throws ParseException {
  Expression p;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case NOT:
      jj_consume_token(NOT);
      p = ExpressionNot(header);
      p = not(p);
      break;
    case LPARENTH:
      jj_consume_token(LPARENTH);
      p = ExpressionOr(header);
      jj_consume_token(RPARENTH);
      break;
    case T:
      p = ExpressionTrue(header);
      break;
    case NUM_INT:
      p = ExpressionIdentifier(header);
      break;
    default:
      jj_la1[17] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    {if (true) return p;}
    throw new Error("Missing return statement in function");
  }

  final public Expression ExpressionTrue(HanoiHeader header) throws ParseException {
  Token literal;
    jj_consume_token(T);
        {if (true) return ExpressionLiteral.getTrue(header.getContext());}
    throw new Error("Missing return statement in function");
  }

  final public Expression ExpressionIdentifier(HanoiHeader header) throws ParseException {
  int id;
    id = parseInt();
    {if (true) return header.numberToIdentifier(id);}
    throw new Error("Missing return statement in function");
  }

  final private int parseInt() throws ParseException {
        Token intg;
    intg = jj_consume_token(NUM_INT);
          {if (true) return Integer.parseInt(intg.toString());}
    throw new Error("Missing return statement in function");
  }

  /** Generated Token Manager. */
  public SpotParserTokenManager token_source;
  SimpleCharStream jj_input_stream;
  /** Current token. */
  public Token token;
  /** Next token. */
  public Token jj_nt;
  private int jj_ntk;
  private int jj_gen;
  final private int[] jj_la1 = new int[18];
  static private int[] jj_la1_0;
  static private int[] jj_la1_1;
  static {
      jj_la1_init_0();
      jj_la1_init_1();
   }
   private static void jj_la1_init_0() {
      jj_la1_0 = new int[] {0xa3e80000,0x20000,0x1c000000,0x200,0x40000000,0x0,0x0,0xa3e80000,0x0,0x2000,0x0,0x8000,0x20000,0x1f00,0x1f00,0x400,0x200,0x900,};
   }
   private static void jj_la1_init_1() {
      jj_la1_1 = new int[] {0x0,0x0,0x0,0x0,0x1000,0x1ff,0x1ff,0x0,0x800,0x0,0x2000,0x0,0x4000,0x4000,0x4000,0x0,0x0,0x3000,};
   }

  /** Constructor with InputStream. */
  public SpotParser(java.io.InputStream stream) {
     this(stream, null);
  }
  /** Constructor with InputStream and supplied encoding */
  public SpotParser(java.io.InputStream stream, String encoding) {
    try { jj_input_stream = new SimpleCharStream(stream, encoding, 1, 1); } catch(java.io.UnsupportedEncodingException e) { throw new RuntimeException(e); }
    token_source = new SpotParserTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 18; i++) jj_la1[i] = -1;
  }

  /** Reinitialise. */
  public void ReInit(java.io.InputStream stream) {
     ReInit(stream, null);
  }
  /** Reinitialise. */
  public void ReInit(java.io.InputStream stream, String encoding) {
    try { jj_input_stream.ReInit(stream, encoding, 1, 1); } catch(java.io.UnsupportedEncodingException e) { throw new RuntimeException(e); }
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 18; i++) jj_la1[i] = -1;
  }

  /** Constructor. */
  public SpotParser(java.io.Reader stream) {
    jj_input_stream = new SimpleCharStream(stream, 1, 1);
    token_source = new SpotParserTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 18; i++) jj_la1[i] = -1;
  }

  /** Reinitialise. */
  public void ReInit(java.io.Reader stream) {
    jj_input_stream.ReInit(stream, 1, 1);
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 18; i++) jj_la1[i] = -1;
  }

  /** Constructor with generated Token Manager. */
  public SpotParser(SpotParserTokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 18; i++) jj_la1[i] = -1;
  }

  /** Reinitialise. */
  public void ReInit(SpotParserTokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 18; i++) jj_la1[i] = -1;
  }

  private Token jj_consume_token(int kind) throws ParseException {
    Token oldToken;
    if ((oldToken = token).next != null) token = token.next;
    else token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    if (token.kind == kind) {
      jj_gen++;
      return token;
    }
    token = oldToken;
    jj_kind = kind;
    throw generateParseException();
  }


/** Get the next Token. */
  final public Token getNextToken() {
    if (token.next != null) token = token.next;
    else token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    jj_gen++;
    return token;
  }

/** Get the specific Token. */
  final public Token getToken(int index) {
    Token t = token;
    for (int i = 0; i < index; i++) {
      if (t.next != null) t = t.next;
      else t = t.next = token_source.getNextToken();
    }
    return t;
  }

  private int jj_ntk() {
    if ((jj_nt=token.next) == null)
      return (jj_ntk = (token.next=token_source.getNextToken()).kind);
    else
      return (jj_ntk = jj_nt.kind);
  }

  private java.util.List<int[]> jj_expentries = new java.util.ArrayList<int[]>();
  private int[] jj_expentry;
  private int jj_kind = -1;

  /** Generate ParseException. */
  public ParseException generateParseException() {
    jj_expentries.clear();
    boolean[] la1tokens = new boolean[47];
    if (jj_kind >= 0) {
      la1tokens[jj_kind] = true;
      jj_kind = -1;
    }
    for (int i = 0; i < 18; i++) {
      if (jj_la1[i] == jj_gen) {
        for (int j = 0; j < 32; j++) {
          if ((jj_la1_0[i] & (1<<j)) != 0) {
            la1tokens[j] = true;
          }
          if ((jj_la1_1[i] & (1<<j)) != 0) {
            la1tokens[32+j] = true;
          }
        }
      }
    }
    for (int i = 0; i < 47; i++) {
      if (la1tokens[i]) {
        jj_expentry = new int[1];
        jj_expentry[0] = i;
        jj_expentries.add(jj_expentry);
      }
    }
    int[][] exptokseq = new int[jj_expentries.size()][];
    for (int i = 0; i < jj_expentries.size(); i++) {
      exptokseq[i] = jj_expentries.get(i);
    }
    return new ParseException(token, exptokseq, tokenImage);
  }

  /** Enable tracing. */
  final public void enable_tracing() {
  }

  /** Disable tracing. */
  final public void disable_tracing() {
  }

}
