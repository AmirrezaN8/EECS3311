package career;

import java.util.*;

public final class CommandLine {
    private CommandLine() {}
    public record Request(String command,List<String> args) {}
    private static final Set<String> GROUPS=Set.of("job","resume","history","interview","checklist");
    public static Request request(List<String> tokens){
        if(tokens.isEmpty())throw new IllegalArgumentException("Enter a command");
        if(GROUPS.contains(tokens.getFirst())){if(tokens.size()<2)throw new IllegalArgumentException("Missing subcommand");return new Request(tokens.get(0)+" "+tokens.get(1),List.copyOf(tokens.subList(2,tokens.size())));}
        return new Request(tokens.getFirst(),List.copyOf(tokens.subList(1,tokens.size())));
    }
    public static List<String> tokenize(String line){
        List<String> tokens=new ArrayList<>();StringBuilder b=new StringBuilder();char quote=0;boolean started=false,escape=false;
        for(char c:line.toCharArray()){
            if(escape){b.append(c);escape=false;started=true;continue;}
            if(c=='\\'&&quote!='\''){escape=true;started=true;continue;}
            if(quote!=0){if(c==quote)quote=0;else b.append(c);started=true;}
            else if(c=='\''||c=='"'){quote=c;started=true;}
            else if(Character.isWhitespace(c)){if(started){tokens.add(b.toString());b.setLength(0);started=false;}}
            else{b.append(c);started=true;}
        }
        if(quote!=0||escape)throw new IllegalArgumentException("Unclosed quote or escape");if(started)tokens.add(b.toString());return tokens;
    }
    public static final String HELP="""
        Career preparation — select a job, import a resume, then run preparation features.
        Quote arguments with spaces. Job/resume text accepts @path to a UTF-8 file.
        job add "TITLE" "COMPANY" CA|US "TEXT or @path"
        job find "TITLE" "COMPANY" CA|US
        job show | job list | job select ID
        resume import PATH | resume paste "TEXT or @path" | resume show | resume versions
        history show | history add CATEGORY "FACT" | history edit ID "FACT" | history delete ID
        chat "PERSONAL CAREER FACT"
        gap | tailor | company
        interview prep | interview start | interview restart
        interview answer "ANSWER" | interview stop | interview feedback
        checklist build | checklist show | checklist tick ITEM_ID [true|false]
        prepare ["GOAL"] | results | help | exit
        Launch Swing: java -jar target/career-agent.jar --gui
        Personal documents are stored locally; configured AI services receive relevant input.
        """;
}
