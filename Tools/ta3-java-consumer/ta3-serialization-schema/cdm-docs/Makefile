LATEX   = pdflatex

REPORT=cdm

TEXSRCS = cdm.tex

all: $(REPORT).pdf

$(REPORT).pdf: $(TEXSRCS)
	$(LATEX) $(REPORT)
	$(LATEX) $(REPORT)

clean:
	rm -f *.dvi *.aux *.log *.blg *.bbl *.lof *.lot *.toc *.nav *.out *.snm *.ps *.pdf

tidy:
	rm -f *.dvi *.aux *.log *.blg *.bbl *.lof *.lot *.toc *.nav *.out *.snm 
