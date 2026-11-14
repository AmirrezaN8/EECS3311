package career;

import java.util.*;
import static career.Models.*;

/** Keeps imported base profiles immutable and stores separately approved, job-linked versions. */
public final class SqliteResumeRepository implements Repository<ResumeProfile> {
    private final SqliteStore store;
    public SqliteResumeRepository(SqliteStore store){this.store=store;}
    private void validateBase(ResumeProfile profile){
        ResumeProfile old=findById(profile.id());if(old!=null&&!old.equals(profile))throw new IllegalStateException("Base resume is immutable; import a new profile instead");
        for(Skill skill:profile.getSkills())if(!normalize(profile.rawText()).contains(normalize(skill.name())))throw new IllegalArgumentException("Skill has no evidence in base resume");
    }
    @Override public void save(ResumeProfile profile){synchronized(store){validateBase(profile);Documents.save(store,"resume",profile.id(),profile);}}
    public void saveAndSelect(ResumeProfile profile){synchronized(store){validateBase(profile);store.saveAll(List.of(new SqliteStore.Write("resume",profile.id(),Documents.encode(profile)),new SqliteStore.Write("settings","resume",Map.of("id",profile.id()))));}}
    @Override public ResumeProfile findById(String id){return Documents.read(store,"resume",required(id,"Resume ID"),ResumeProfile.class);}
    @Override public List<ResumeProfile> findAll(){return Documents.all(store,"resume",ResumeProfile.class);}
    public ResumeProfile current(){var selected=store.find("settings","resume");if(selected==null)throw new IllegalStateException("Import or paste a resume first");var profile=findById(Json.text(selected,"id"));if(profile==null)throw new IllegalStateException("Selected resume is missing; import or paste it again");return profile;}
    @Override public void delete(String id){synchronized(store){if(findById(id)==null)throw new IllegalArgumentException("Unknown resume ID");if(Documents.all(store,"version",ResumeVersion.class).stream().anyMatch(v->v.baseResumeId().equals(id)))throw new IllegalStateException("Resume has saved tailored versions; deletion would orphan them");Map<String,String> keys=new LinkedHashMap<>();keys.put("resume",id);var selected=store.find("settings","resume");if(selected!=null&&id.equals(selected.get("id")))keys.put("settings","resume");store.deleteAll(keys);}}
    public void save(ResumeVersion version){synchronized(store){
        new SqliteJobRepository(store).require(version.jobId());if(findById(version.baseResumeId())==null)throw new IllegalArgumentException("Unknown base resume ID");
        ResumeVersion old=findVersion(version.id());if(old!=null&&!old.equals(version))throw new IllegalStateException("Saved resume version is immutable");Documents.save(store,"version",version.id(),version);
    }}
    public ResumeVersion findVersion(String id){return Documents.read(store,"version",id,ResumeVersion.class);}
    public List<ResumeVersion> findVersions(String jobId){return Documents.all(store,"version",ResumeVersion.class).stream().filter(v->v.jobId().equals(jobId)).toList();}
}
