# InstrumentationJDT

## How to run

This is a maven project, so, you can import it in IDEA and run from the Main.java

**Options:**

-d4jhome: the path of checked Defects4J bug, example `/Path/to/project/tmp/defects4j_buggy/` 
-pip: the project information path, example: `./d4j-info/patches_inputs.csv`

The checked bug path should be like this: `d4jhome/bug_project/bugProject_bugIdnum`

More options are set in [Constant file](https://github.com/Feng-Jay/InstrumentationJDT/blob/master/src/main/java/utils/Constant.java)

**Output format**

For **If-Condition**:
```
PackageName#LineNumber#IF-CONDITION#exprString
subexpr1 : value
subexpr2 : value
...
```

For **Return-Value**:
```
PackageName#LineNumber#RETURN-VALUE#exprString
subexpr1 : value
subexpr2 : value
...
```

For **Return-Object**:
```
PackageName#LineNumber#RETURN-OBJECT#exprString
subexpr1 is NULL: true or false
subexpr2 is NULL: true or false
...
```


