# Safe demonstration inputs

All names, qualifications and the posting here are fictional inputs, not AI outputs or an actual vacancy. They contain no credentials or personal contact information. Resume variants exercise different parsers; the PDF has a shorter profile than the Markdown/DOCX versions.

- job-description.txt: pasted-source F01 input with explicitly labeled ISO deadline.
- resume.md: F02 Markdown/pasted extraction input.
- resume.docx: a small Word Open XML package for F02 file import.
- resume.pdf: a small text PDF for Maven/PDFBox import verification.

Example CLI operations after building:

```sh
java -jar target/career-agent.jar job add "Junior Java Developer" "Example Company" CA @samples/job-description.txt
java -jar target/career-agent.jar resume import samples/resume.md
java -jar target/career-agent.jar resume import samples/resume.docx
```

Resume imports invoke real configured Haiku. Check the profile's `warning` is empty before claiming structured extraction works. Raw-fallback success is labeled and demonstrates error handling, not successful AI extraction.

For PDF and legacy Word support use `mvn test package` and launch `target/career-agent-2.0.0.jar`. No binary .doc success fixture is included; use a non-sensitive real .doc for that remaining manual check.

Do not search Adzuna for the fictional Example Company and claim it is a real posting. Use an actual title/employer for the live lookup; show the returned partial source and its attribution.
