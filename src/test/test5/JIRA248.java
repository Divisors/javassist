package test5;

interface JIRA248Intf {
    default int foo() { return 1; }
}

class JIRA248Sup implements JIRA248Intf {
}

public class JIRA248 extends JIRA248Sup {
    public int foo() { return 70; }
}