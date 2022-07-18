package com.deens.cheese;

public class SaleTargetClass {

    private String Result;

    private String ResultColour;

    private String DateFrom;

    private String DateTo;

    private String MonthName;

    private String TotalDays;

    private String PassedDays;

    private String RemainingDays;

    private String SaleTarget;

    private String AchieveTarget;

    private String RemainingTarget;

    private String PerDayTarget;

    private String CurrentTargetRatio;

    private String RequiredTargetPerday;

    public void setResult(String Result){
        this.Result = Result;
    }
    public String getResult(){
        return this.Result;
    }
    public void setResultColour(String ResultColour){
        this.ResultColour = ResultColour;
    }
    public String getResultColour(){
        return this.ResultColour;
    }
    public String getDateFrom() {
        return DateFrom;
    }
    public void setDateFrom(String dateFrom) {
        DateFrom = dateFrom;
    }
    public String getDateTo() {
        return DateTo;
    }
    public void setDateTo(String dateTo) {
        DateTo = dateTo;
    }
    public void setMonthName(String MonthName){
        this.MonthName = MonthName;
    }
    public String getMonthName(){
        return this.MonthName;
    }
    public void setTotalDays(String TotalDays){
        this.TotalDays = TotalDays;
    }
    public String getTotalDays(){
        return this.TotalDays;
    }
    public void setPassedDays(String PassedDays){
        this.PassedDays = PassedDays;
    }
    public String getPassedDays(){
        return this.PassedDays;
    }
    public void setRemainingDays(String RemainingDays){
        this.RemainingDays = RemainingDays;
    }
    public String getRemainingDays(){
        return this.RemainingDays;
    }
    public void setSaleTarget(String SaleTarget){
        this.SaleTarget = SaleTarget;
    }
    public String getSaleTarget(){
        return this.SaleTarget;
    }
    public void setAchieveTarget(String AchieveTarget){
        this.AchieveTarget = AchieveTarget;
    }
    public String getAchieveTarget(){
        return this.AchieveTarget;
    }
    public void setRemainingTarget(String RemainingTarget){
        this.RemainingTarget = RemainingTarget;
    }
    public String getRemainingTarget(){
        return this.RemainingTarget;
    }
    public void setPerDayTarget(String PerDayTarget){
        this.PerDayTarget = PerDayTarget;
    }
    public String getPerDayTarget(){
        return this.PerDayTarget;
    }
    public void setCurrentTargetRatio(String CurrentTargetRatio){
        this.CurrentTargetRatio = CurrentTargetRatio;
    }
    public String getCurrentTargetRatio(){
        return this.CurrentTargetRatio;
    }
    public void setRequiredTargetPerday(String RequiredTargetPerday){
        this.RequiredTargetPerday = RequiredTargetPerday;
    }
    public String getRequiredTargetPerday(){
        return this.RequiredTargetPerday;
    }

    public SaleTargetClass(String result, String resultColour, String dateFrom, String dateTo, String monthName,
                           String totalDays, String passedDays, String remainingDays, String saleTarget,
                           String achieveTarget, String remainingTarget, String perDayTarget, String currentTargetRatio, String requiredTargetPerday) {
        Result = result;
        ResultColour = resultColour;
        DateFrom = dateFrom;
        DateTo = dateTo;
        MonthName = monthName;
        TotalDays = totalDays;
        PassedDays = passedDays;
        RemainingDays = remainingDays;
        SaleTarget = saleTarget;
        AchieveTarget = achieveTarget;
        RemainingTarget = remainingTarget;
        PerDayTarget = perDayTarget;
        CurrentTargetRatio = currentTargetRatio;
        RequiredTargetPerday = requiredTargetPerday;
    }
}
