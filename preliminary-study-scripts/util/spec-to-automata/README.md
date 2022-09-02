# Spec to Automata
This directory contains a very simple tool that converts LTL and ERE formulas to FSMs.

## Dependencies
You need to have installed maven, and have dk.brics:automaton:jar:1.12-4 in your M2 repository.
To have the latter, clone this repo elsewhere: https://github.com/cs-au-dk/dk.brics.automaton.git and run `mvn install -Dgpg.skip`.

## Using the tool
One can run the tool via the following command:
```
bash run-spec2automata.sh EVENTS_FILE FORMULA_FILE
```
where
1. `EVENTS_FILE` is a file listing the events in the specification, separated by newline and allows comments via `#`. (ex. `sample-inputs-new/new-hasNext-events.txt`)
2. `FORMULA_FILE` is a 1-line file listing the LTL formula to be converted into a FSM. (ex. `sample-inputs-new/new-hasNext-formula.txt`)

ex)
```
bash run-spec2automata.sh sample-inputs-new/new-hasNext-events.txt sample-inputs-new/new-hasNext-formula.txt
```

Note that the current version only works for converting LTL specifications to FSMs.

## Reading the Output
`run-spec2automata.sh` produces three files:
1. `dotFile-*.dot` - a `*.dot` file representing the corresponding FSM
2. `dotFile-*.png` - a `*.png` file representating the corresponding FSM
3. `transitionKey-*.txt` - a `*.txt` file containing a mapping between FSM alphabet letters and the events that they represent
