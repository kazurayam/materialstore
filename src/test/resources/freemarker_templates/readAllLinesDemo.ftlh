<#-- readAllLinesDemo.ftlh -->
<#-- custom directive name "readAllLines" is defined as a shared variable. See TestBase.java -->
<#assign x = 0>
<@readAllLines path="AmznPress/20220310_203757/objects/e96bd4c2e345301b567d70071dcec04fda699ce4.csv"; line>
    <tr><td>${x}</td><td>${line}</td></tr>
    <#assign x++>
</@readAllLines>