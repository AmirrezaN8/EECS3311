package career;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

public final class CliApp implements AgentController.UserInteraction {
    private final Scanner input;
    public CliApp(){this(new Scanner(System.in));}
    private CliApp(Scanner input){this.input=input;}
    public boolean askApproval(String prompt){System.out.println(prompt+" [y/N]");return input.hasNextLine()&&input.nextLine().strip().equalsIgnoreCase("y");}
    public String readText(String prompt){System.out.println(prompt+":");return input.hasNextLine()?input.nextLine():"";}
    public static void main(String[] args){
        if(args.length==1&&args[0].equals("--gui")){SwingGui.main(new String[0]);return;}
        if(args.length==1&&(args[0].equals("help")||args[0].equals("--help"))){System.out.println(CommandLine.HELP);return;}
        try{new CliApp().run(args);}catch(RuntimeException e){System.err.println("Error: "+e.getMessage());System.exit(1);}
    }
    public void run(String[] args){
        if(args.length>0 && args[0].equals("-input")){
            if(args.length!=2)throw new IllegalArgumentException("Usage: -input <file>");
            String task;
            try{task=Files.readString(Path.of(args[1])).strip();}
            catch(IOException e){throw new IllegalArgumentException("Cannot read task file: "+args[1],e);}
            if(task.isEmpty() || task.lines().count()!=1)
                throw new IllegalArgumentException("Task file must contain one command on one line");
            new CliApp(new Scanner("")).runCommand(CommandLine.tokenize(task));
            return;
        }
        if(args.length>0){runCommand(List.of(args));return;}
        AgentController c=controller();
        System.out.println(CommandLine.HELP);
        while(true){System.out.print("career> ");if(!input.hasNextLine())return;String line=input.nextLine().strip();if(line.isEmpty())continue;if(line.equals("exit"))return;
            try{execute(c,CommandLine.tokenize(line));}catch(RuntimeException e){System.err.println("Error: "+e.getMessage());}
        }
    }
    private AgentController controller(){
        AgentController c=new AgentController(new Config(),this);
        c.events.attach(e->System.out.println("["+e.type()+"] "+e.message()));
        return c;
    }
    private void runCommand(List<String> tokens){execute(controller(),tokens);}
    private static void execute(AgentController c,List<String> tokens){if(tokens.size()==1&&tokens.getFirst().equals("help")){System.out.println(CommandLine.HELP);return;}var r=CommandLine.request(tokens);System.out.print(Display.format(c.runFeature(r.command(),r.args())));}
}
