# Prompto

A coding assistant based on ChatGPT.

The core concept of Prompto is to capture your IDE state in order to contextualize queries you send to ChatGPT. This allows the generated responses to be fine-tuned to your use-case, building on the code present in your project. Prompto has been mainly used and tested on Java projects but can also work on other languages in a limited way.

**Be mindful of potential issues with private or sensitive data.** Each action lists what type of data it sends to the LLM.

To use it, fill in your API key in Prompto settings and press Alt-Enter in the editor to be presented with prompto actions.

## General use

Starting a Prompto action opens a dialog which shows which context information to add to your prompt. You can then either send the query to the LLM or export it to your clipboard.

## Prompto implement

Use this action on an existing method to implement or modify it. Prompto will use the relevant type definitions of your project to build an optimal implementation. This is best used on code like converter methods, test cases or usage of external (and well-known) libraries. This is a Java only feature.

## Prompto ask

Use this action to ask a quick question about the code. This is more for flavor but can be sometimes useful. You can also ask Prompto how it's going, I guess it's a feature ¯\_(ツ)_/¯

## Prompto sql - Intellij Ultimate only

In a database console, ask Prompto to generate a SQL script. Prompto will have access to your database schema to generate a relevant SQL query. Note that this feature currently only works for small databases. (~40 tables max)

Feedback is welcome at https://github.com/vcque/prompto. Have fun!