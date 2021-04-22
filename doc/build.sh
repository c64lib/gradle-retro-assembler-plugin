#!/bin/bash

bundle exec asciidoctor -D out -r asciidoctor-diagram index.adoc
mkdir out/diagrams
cp include/diagrams/*.svg out/diagrams
#bundle exec asciidoctor -D out -r asciidoctor-kroki index.adoc
