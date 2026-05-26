package com.sc.dto.request;

public class NoticeStatsDTO {

    private long total;
    private long active;
    private long draft;
    private long expired;

    public NoticeStatsDTO() {}

    public NoticeStatsDTO(long total, long active, long draft, long expired) {
        this.total   = total;
        this.active  = active;
        this.draft   = draft;
        this.expired = expired;
    }

    public long getTotal()   { return total; }
    public void setTotal(long total) { this.total = total; }

    public long getActive()  { return active; }
    public void setActive(long active) { this.active = active; }

    public long getDraft()   { return draft; }
    public void setDraft(long draft) { this.draft = draft; }

    public long getExpired() { return expired; }
    public void setExpired(long expired) { this.expired = expired; }
}
