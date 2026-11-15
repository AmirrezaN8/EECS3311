package career;

import java.util.*;
import static career.Models.*;

public final class ChecklistService {
    private final Ai ai;private final SqliteChecklistRepository repository;private final SqliteJobRepository jobs;private final SqliteResumeRepository resumes;
    public ChecklistService(Ai ai,SqliteStore store){this.ai=ai;this.repository=new SqliteChecklistRepository(store);this.jobs=new SqliteJobRepository(store);this.resumes=new SqliteResumeRepository(store);}
    public Checklist build(JobDescription job){
        List<String> labels=new ArrayList<>();List<ResumeVersion> versions=resumes.findVersions(job.id());
        labels.add(versions.stream().anyMatch(v->v.jobId().equals(job.id())&&v.changes().stream().anyMatch(ResumeChange::accepted))?"Review accepted tailored resume for "+job.title():"Tailor and review resume for "+job.title());
        if(job.deadline()!=null)labels.add("Apply by posting deadline: "+job.deadline());
        SkillGapReport gaps=jobs.findSkillGap(job.id());
        if(gaps==null)labels.add("Identify missing skills (analysis not yet available)");else for(var a:gaps.groupedByPriority())if(a.group()!=GapGroup.STRONG)labels.add("Practice "+a.skillName()+(a.uncertain()?" (assessment uncertain)":""));
        labels.add(!jobs.hasResult(job.id(),"questions")?"Generate tailored interview questions":"Practice saved interview questions");
        labels.add(!jobs.hasResult(job.id(),"company")?"Research company goals and values":"Review sourced company summary");
        List<String> ordered=labels;
        try{ordered=ai.ask("Order these exact checklist labels by priority: resume match, explicit deadline, missing skills, interview prep, company. Return {\"items\":[\"unchanged label\"]}.",labels,ModelTier.SONNET,m->{
            List<String> list=Json.strings(m,"items");if(list.size()!=labels.size()||!new HashSet<>(list).equals(new HashSet<>(labels)))throw new IllegalArgumentException("Checklist labels changed");return list;
        });}catch(RuntimeException e){if(Thread.currentThread().isInterrupted())throw e;/* Deterministic priority order remains useful without an LLM. */}
        Checklist old=repository.findById(job.id());Map<String,ChecklistItem> prior=new HashMap<>();if(old!=null)old.items().forEach(i->prior.put(i.label(),i));
        List<ChecklistItem> items=new ArrayList<>();for(String label:ordered){ChecklistItem before=prior.get(label);items.add(new ChecklistItem(before==null?id():before.id(),label,items.size()+1,before!=null&&before.done()));}
        Checklist list=new Checklist(old==null?id():old.id(),job.id(),job.deadline(),items);repository.save(list);return list;
    }
    public Checklist tick(String jobId,String itemId,boolean done){return repository.tick(jobId,itemId,done);}
}
