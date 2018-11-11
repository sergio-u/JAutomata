import guru.nidi.graphviz.attribute.Label;
import guru.nidi.graphviz.attribute.RankDir;
import guru.nidi.graphviz.attribute.Shape;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.*;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.*;
import java.awt.event.*;  

import static guru.nidi.graphviz.model.Factory.*;


public class Automaton{

    static Scanner scanner = new Scanner(System.in);
    String emptySymbol = "_";
    LinkedHashSet<Character> alphabet;
    Set<String> states;
    Set<String> finalStates;
    String initialState;

    // ∂(qo, a) = {q1}
    // ∂(qo, €) = {q1, q2}
    Map<String, Map<Character, Set<String>>> transitions;


    public Automaton() {
        alphabet = new LinkedHashSet<>();
        states = new HashSet<>();
        finalStates = new HashSet<>();
        initialState = "";
        transitions = new HashMap<>();
    }

    public void parseTransitionFunctionsRegex(List<String> lines) {
        Pattern pattern = Pattern.compile("(->|\\*)?\\s*(\\w*)\\s*(\\w*)\\s*(\\w*)");
        for (String c : lines) {
            Matcher matcher = pattern.matcher(c);
            if (!matcher.matches()) continue;
            String fromState = matcher.group(2);
            char symbol = matcher.group(3).charAt(0);

            // Has special character for empty set
            String toState = matcher.group(4);

            if (matcher.group(1) != null) {
                switch (matcher.group(1)) {
                    case "->":
                        this.initialState = fromState;
                        break;
                    case "*":
                        this.finalStates.add(fromState);
                        break;
                }
            }
            states.add(fromState);
            alphabet.add(symbol);
            transitions.putIfAbsent(fromState, new HashMap<>());
            transitions.get(fromState).putIfAbsent(symbol, new HashSet<>());
            transitions.get(fromState).get(symbol).add(toState);
        }
    }

    public Automaton toDFA() {
        return NFAToDFA.convert(this);
    }

    public void viewAutomaton() throws IOException {
        Graph graph = createGraph();
        File tmpImage = File.createTempFile("tmp", ".png", new File("images/"));
        Graphviz.fromGraph(graph).height(1000).render(Format.PNG).toFile(tmpImage);
        Desktop desktop = Desktop.getDesktop();
        desktop.open(tmpImage);
        //tmpImage.deleteOnExit();
    }

    //node("a").link(to(node("b")).with(Label.of("Test"))),
    public Graph createGraph() {
        //getNodes(this.initialState);
        List<MutableNode> nodes = getNodes();
        Graph g = graph("example2").directed()
                .graphAttr().with(RankDir.LEFT_TO_RIGHT);
        for (MutableNode node : nodes) {
            g = g.with(node);
        }
        return g;
    }


    public List<MutableNode> getNodes() {
        Map<String, MutableNode> nodes = new HashMap<>();
        transitions.entrySet().stream().forEach(c -> {
//            System.out.println("1: "+c);
            getNodes(c.getKey(), nodes);
        });
        return new ArrayList<>(nodes.values());
    }

    public void getNodes(String state, Map<String, MutableNode> nodes) {
        transitions.get(state).entrySet().stream()
                .forEach(c -> {
                    System.out.println("2: "+c);
                    getNodes(state, c.getKey(), c.getValue(), nodes);
                });
    }

    //node("a").link(to(node("b")).with(Label.of("Test"))),
    public void getNodes(String from, Character input, Set<String> states, Map<String, MutableNode> nodes) {
        System.out.printf("3: %s %s %s\n", from, input, states);
        nodes.putIfAbsent(from, mutNode(from));
        if (from.equals(this.initialState)){
            MutableNode init = mutNode("").add(Shape.POINT).addLink(nodes.get(from));
            nodes.putIfAbsent("", init);
        }
        if (this.finalStates.contains(from)) {
            nodes.get(from).add(Shape.DOUBLE_CIRCLE);
        }
        states.stream().filter(to -> !to.equals(emptySymbol)).forEach(to -> {
            // Find before inserting
            Optional<Link> tmp = nodes.get(from).links().stream().filter(l -> {
                Boolean p = l.asLinkSource().toString().equals(to);
                return p;
            }).findAny();
            if (tmp.isPresent()) {
                String label = tmp.get().attrs().get("label").toString();
                tmp.get().attrs().add("label",label+","+input );
            }else {
                nodes.get(from).addLink(to(node(to)).with(Label.of("" + input)));
            }
            System.out.println("4: "+to);
        });
//        nodes.get(from).addLink(states.stream().filter(c -> !c.equals(emptySymbol)).toArray(String[]::new));
    }



    public static void main(String[] args) throws IOException {
        //Input GUI set up
        JFrame frame = new JFrame("Automaton GUI");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500,200);
        //center middle of the window
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        JPanel panel1 = new JPanel();
        JPanel panel2 = new JPanel();

        JLabel label1 = new JLabel("Enter transition (a 0 b)");
        JTextField txtfield1 = new JTextField(6);
        JButton addBtn = new JButton("Add transition");
        JButton endBtn = new JButton("Create Automaton");
        //addBtn.addActionListener(this);
        //endBtn.addActionListener(this);

        panel1.add(label1);
        panel1.add(txtfield1);
        panel2.add(addBtn);
        panel2.add(endBtn);

        frame.getContentPane().add(BorderLayout.NORTH,panel1);
        frame.getContentPane().add(BorderLayout.SOUTH,panel2);

        frame.setVisible(true);



        Automaton test = new Automaton();

        //test.parseAlphabet("a b c d e f g");

        //test 1
        //String f1 = "->a 0 a";
        //String f2 = "->a 0 b";
        //String f3 = "->a 0 c";
        //String f4 = "->a 0 d";
        //String f5 = "->a 0 e";
        //String f6 = "->a 1 d";
        //String f7 = "->a 1 e";
        //String f8 = "b 0 c";
        //String f9 = "b 1 e";
        //String f10 = "c 0 _";
        //String f11 = "c 1 b";
        //String f12 = "d 0 e";
        //String f13 = "d 1 _";
        //String f14 = "*e 0 _";
        //String f15 = "*e 1 _";
        //String[] tmp = new String[]{f1, f2, f3, f4, f5, f6, f7, f8, f9, f10, f11, f12, f13, f14, f15};
        //List<String> fs = new ArrayList<>(Arrays.asList(tmp));

        //test 2 https://www.geeksforgeeks.org/theory-of-computation-conversion-from-nfa-to-dfa/
        //String f1 = "->a 0 a";
        //String f2 = "->a 1 a";
        //String f3 = "->a 0 b";
        //String f4 = "b 1 c";
        //String f5 = "*c 0 _";
        //String f6 = "*c 1 _";
        //String[] tmp = new String[]{f1, f2, f3, f4, f5, f6};
        //List<String> fs = new ArrayList<>(Arrays.asList(tmp));

        //No se si esta mal es parseo pero no sale
        //test 3 https://er.yuvayana.org/nfa-to-dfa-conversion-algorithm-with-solved-example/
        //String f1 = "->a 0 c";
        //String f2 = "b 1 c";
        //String f3 = "b 1 a";
        //String f4 = "c 0 a";
        //String f5 = "c 1 a";
        //String f6 = "c 0 b";
        //String f7 = "*c 0 _";
        //String f8 = "*c 1 _";
        //String[] tmp = new String[]{f1, f2, f3, f4, f5, f6, f7, f8};
        //List<String> fs = new ArrayList<>(Arrays.asList(tmp));

        //No se puede probar con inicial = estado final
        //test 4 https://www.cs.odu.edu/~toida/nerzic/390teched/regular/fa/nfa-2-dfa.html
        //String f1 = "*->a 0 a";
        //String[] tmp = new String[]{f1, f2, f3, f4, f5, f6, f7, f8, f9, f10, f11, f12, f13, f14, f15};
        //List<String> fs = new ArrayList<>(Arrays.asList(tmp));

        //List<String> fs = new ArrayList<>();
        addBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String trn = "";
                trn = txtfield1.getText();
                System.out.println("Add transition pressed and read: "+trn);
                fs.add(trn);
            }
        });

        endBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent w) {
                try {
                    System.out.println("Create automaton");
                    test.parseTransitionFunctionsRegex(fs);
                    //test.viewAutomaton();
                    Automaton DFA = test.toDFA();
                    DFA.viewAutomaton();
                }catch(IOException e) {
                    System.out.println("IOException");
                }
            }
        });

        ////test.parseTransitionFunctions(fs);
        //test.parseTransitionFunctionsRegex(fs);
        ////System.out.println("Done");
        ////test.viewAutomaton();
        //Automaton DFA = test.toDFA();
        //DFA.viewAutomaton();
    }
}

