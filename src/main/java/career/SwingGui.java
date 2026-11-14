package career;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Supplier;
import static career.Models.*;

public final class SwingGui implements AgentController.UserInteraction {
    private final JFrame frame=new JFrame("Career Preparation Agent — EECS3311");
    private final JTextArea output=area(15),trace=area(5),jobText=area(12),resumeText=area(12),answer=area(6);
    private final JTextField title=new JTextField(20),company=new JTextField(20),goal=new JTextField("prepare me for this job",35),factId=new JTextField(36),factText=new JTextField(35);
    private final JComboBox<String> country=new JComboBox<>(new String[]{"CA","US"});
    private final JPanel checklistPanel=new JPanel();private final JProgressBar progress=new JProgressBar(0,100);
    private final List<AbstractButton> controls=new ArrayList<>();private final JLabel status=new JLabel("Ready");
    private AgentController controller;private SwingWorker<Object,Void> worker;
    private final java.util.concurrent.atomic.AtomicBoolean running=new java.util.concurrent.atomic.AtomicBoolean();
    private static JTextArea area(int rows){JTextArea a=new JTextArea(rows,65);a.setLineWrap(true);a.setWrapStyleWord(true);return a;}
    public static void main(String[] args){
        if(GraphicsEnvironment.isHeadless()){System.err.println("GUI needs a desktop display; launch the CLI instead.");return;}
        SwingUtilities.invokeLater(()->{try{UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());new SwingGui().show();}catch(Exception e){JOptionPane.showMessageDialog(null,e.getMessage(),"Startup failed",JOptionPane.ERROR_MESSAGE);}});
    }
    private void show(){
        controller=new AgentController(new Config(),this);controller.events.attach(e->SwingUtilities.invokeLater(()->{trace.append("["+e.type()+"] "+e.message()+"\n");trace.setCaretPosition(trace.getDocument().getLength());}));
        output.setEditable(false);trace.setEditable(false);
        JTabbedPane tabs=new JTabbedPane();tabs.addTab("Job",jobs());tabs.addTab("Resume",resumes());tabs.addTab("My History",history());tabs.addTab("Preparation",preparation());tabs.addTab("Mock Interview",interviews());tabs.addTab("Checklist",checklists());
        JPanel results=new JPanel(new BorderLayout());results.add(new JLabel("Results and errors"),BorderLayout.NORTH);results.add(new JScrollPane(output));results.add(new JScrollPane(trace),BorderLayout.SOUTH);
        JSplitPane split=new JSplitPane(JSplitPane.VERTICAL_SPLIT,tabs,results);split.setResizeWeight(.60);
        JPanel top=new JPanel(new BorderLayout());top.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));top.add(new JLabel("Documents stay in local SQLite. Relevant text is sent to your configured AI/search services."),BorderLayout.NORTH);top.add(split);
        JButton stop=new JButton("Stop / cancel");stop.addActionListener(e->{controller.stop();if(worker!=null)worker.cancel(true);});JPanel bottom=new JPanel(new BorderLayout());bottom.add(status);bottom.add(stop,BorderLayout.EAST);top.add(bottom,BorderLayout.SOUTH);
        frame.setContentPane(top);frame.setSize(1050,900);frame.setMinimumSize(new Dimension(760,650));frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter(){public void windowClosing(WindowEvent e){if(worker!=null&&!worker.isDone()&&!askApproval("An operation is running. Stop and close? Saved work is retained."))return;controller.stop();if(worker!=null)worker.cancel(true);frame.dispose();}});
        frame.setLocationByPlatform(true);frame.setVisible(true);
    }
    private JPanel jobs(){
        JPanel p=new JPanel(new BorderLayout());JPanel fields=new JPanel(new GridLayout(0,2,8,5));fields.add(new JLabel("Job title"));fields.add(title);fields.add(new JLabel("Company"));fields.add(company);fields.add(new JLabel("Country"));fields.add(country);p.add(fields,BorderLayout.NORTH);p.add(new JScrollPane(jobText));jobText.getAccessibleContext().setAccessibleName("Pasted job description");
        JPanel buttons=new JPanel();buttons.add(button("Save pasted posting",()->call("job add",title.getText(),company.getText(),country.getSelectedItem().toString(),jobText.getText())));buttons.add(button("Find via Adzuna",()->call("job find",title.getText(),company.getText(),country.getSelectedItem().toString())));buttons.add(button("Show current job",()->call("job show")));buttons.add(button("Select saved job",()->{
            var value=JOptionPane.showInputDialog(frame,"Saved job ID (use List jobs first)");if(value!=null)call("job select",value);
        }));buttons.add(button("List jobs",()->call("job list")));p.add(buttons,BorderLayout.SOUTH);return p;
    }
    private JPanel resumes(){JPanel p=new JPanel(new BorderLayout());p.add(new JLabel("Paste resume text, or import PDF / DOCX / Markdown / UTF-8 text"),BorderLayout.NORTH);p.add(new JScrollPane(resumeText));resumeText.getAccessibleContext().setAccessibleName("Resume text");JPanel b=new JPanel();b.add(button("Save and extract profile",()->call("resume paste",resumeText.getText())));b.add(button("Import file",()->{JFileChooser chooser=new JFileChooser();if(chooser.showOpenDialog(frame)==JFileChooser.APPROVE_OPTION)call("resume import",chooser.getSelectedFile().getAbsolutePath());}));b.add(button("Show profile",()->call("resume show")));b.add(button("Tailored versions",()->call("resume versions")));p.add(b,BorderLayout.SOUTH);return p;}
    private JPanel history(){JPanel p=new JPanel(new BorderLayout());JPanel fields=new JPanel(new GridLayout(0,2,8,5));fields.add(new JLabel("Fact ID (for edit/delete; shown in history)"));fields.add(factId);fields.add(new JLabel("Fact / personal career message"));fields.add(factText);p.add(fields,BorderLayout.NORTH);JPanel b=new JPanel();b.add(button("Show history",()->call("history show")));b.add(button("Remember message (AI)",()->call("chat",factText.getText())));b.add(button("Add fact directly",()->call("history add","user",factText.getText())));b.add(button("Edit fact",()->call("history edit",factId.getText(),factText.getText())));b.add(button("Delete fact",()->call("history delete",factId.getText())));p.add(b,BorderLayout.SOUTH);return p;}
    private JPanel preparation(){JPanel p=new JPanel(new BorderLayout());JPanel buttons=new JPanel();buttons.add(button("Identify skill gaps",()->call("gap")));buttons.add(button("Tailor resume",()->call("tailor")));buttons.add(button("Research company",()->call("company")));buttons.add(button("Generate interview questions",()->call("interview prep")));buttons.add(button("Saved results",()->call("results")));p.add(buttons,BorderLayout.NORTH);JPanel g=new JPanel();g.add(new JLabel("Goal"));g.add(goal);g.add(button("Prepare me",this::submitGoal));p.add(g,BorderLayout.CENTER);JTextArea help=area(5);help.setEditable(false);help.setText("Start by saving a job and resume. Individual feature buttons run directly. Prepare me uses an AI planner, saved history and approved steps to build a checklist. Review every proposed resume edit. Progress events appear below. Stop cancels outstanding work; saved results remain available.");p.add(help,BorderLayout.SOUTH);return p;}
    public void submitGoal(){call("prepare",goal.getText());}
    private JPanel interviews(){JPanel p=new JPanel(new BorderLayout());p.add(new JLabel("Answer one question at a time. Feedback appears after completion or approved early stop."),BorderLayout.NORTH);p.add(new JScrollPane(answer));answer.getAccessibleContext().setAccessibleName("Typed mock interview answer");JPanel b=new JPanel();b.add(button("Start / continue",()->call("interview start")));b.add(button("Submit answer",()->{String text=answer.getText();call("interview answer",text);}));b.add(button("Stop and save",()->call("interview stop")));b.add(button("Final feedback",()->call("interview feedback")));b.add(button("New attempt",()->call("interview restart")));p.add(b,BorderLayout.SOUTH);return p;}
    private JPanel checklists(){JPanel p=new JPanel(new BorderLayout());JPanel b=new JPanel();b.add(button("Build / refresh checklist",()->call("checklist build")));b.add(button("Show saved checklist",()->call("checklist show")));p.add(b,BorderLayout.NORTH);checklistPanel.setLayout(new BoxLayout(checklistPanel,BoxLayout.Y_AXIS));p.add(new JScrollPane(checklistPanel));progress.setStringPainted(true);p.add(progress,BorderLayout.SOUTH);return p;}
    public void showChecklist(Checklist list){
        checklistPanel.removeAll();progress.setValue(list.progress());
        for(var item:list.items()){JCheckBox tick=new JCheckBox(item.priority()+". "+item.label(),item.done());tick.addActionListener(e->call("checklist tick",item.id(),Boolean.toString(tick.isSelected())));checklistPanel.add(tick);}
        checklistPanel.revalidate();checklistPanel.repaint();
    }
    private JButton button(String label,Runnable action){JButton b=new JButton(label);b.addActionListener(e->action.run());controls.add(b);return b;}
    private void call(String command,String... args){List<String> captured=List.of(args);run(()->controller.runFeature(command,captured));}
    private void run(Supplier<Object> operation){
        if(running.get()||(worker!=null&&!worker.isDone())){JOptionPane.showMessageDialog(frame,"Wait for the current operation or press Stop.");return;}
        controls.forEach(b->b.setEnabled(false));status.setText("Working…");
        worker=new SwingWorker<>(){
            protected Object doInBackground(){running.set(true);try{return operation.get();}finally{running.set(false);}}
            protected void done(){try{Object result=get();output.setText(Display.format(result));if(result instanceof Checklist c)showChecklist(c);status.setText("Ready");}
                catch(CancellationException e){output.setText("Operation cancelled. Completed work remains saved.");status.setText("Cancelled");}
                catch(Exception e){Throwable cause=e instanceof ExecutionException?e.getCause():e;output.setText("Error: "+cause.getMessage());status.setText("Failed — see error");}
                finally{controls.forEach(b->b.setEnabled(true));}}
        };worker.execute();
    }
    public boolean askApproval(String prompt){return onEdt(()->JOptionPane.showConfirmDialog(frame,scrollMessage(prompt),"Approval required",JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE)==JOptionPane.YES_OPTION,false);}
    public String readText(String prompt){return onEdt(()->{JTextArea text=area(10);int result=JOptionPane.showConfirmDialog(frame,new Object[]{prompt,new JScrollPane(text)},"Input required",JOptionPane.OK_CANCEL_OPTION);return result==JOptionPane.OK_OPTION?text.getText():"";},"");}
    private static Object scrollMessage(String prompt){JTextArea text=area(12);text.setText(prompt);text.setEditable(false);text.setCaretPosition(0);return new JScrollPane(text);}
    private static <T> T onEdt(Callable<T> task,T fallback){
        if(Thread.currentThread().isInterrupted())return fallback;
        if(SwingUtilities.isEventDispatchThread())try{return task.call();}catch(Exception e){return fallback;}
        FutureTask<T> f=new FutureTask<>(task);SwingUtilities.invokeLater(f);
        try{return f.get();}catch(InterruptedException e){f.cancel(false);Thread.currentThread().interrupt();return fallback;}catch(Exception e){return fallback;}
    }
}
