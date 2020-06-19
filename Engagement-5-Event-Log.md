# Introduction
This page captures the operational event log.  It is here to help identify operational issues and their impact.  The log is organized by day, with a summary for each day and a list of events.  Each event takes the form:
### **What:** What actually happened
  * **Start time:** Roughly when it started
  * **End time:** Roughly when it ended
  * **Impact:** How did it impact the engagement
  * **Cause:** What was the root cause (or closest thing to a root cause)
  * **Resolution:** How was the issue resolved


# 2019-05-06
## Summary
The engagement was pushed back one day to allow for more final setup and testing.

## Events
None

# 2019-05-07
## Summary
We had some issues with THEIA when we started it up and needed to change topics.  Later in the day with THEIA, we needed to also turn off the recording/replay functionality in the afternoon.

We also needed to change topics for CADETS due an issue with duplicate records in the original topic.  The new topic has all data from the beginning of the day, and it is expected to be up to real time going forward.

We had some reboots of ta1-fivedirections-2 and ta1-marple-1, but the hosts are back up and publishing to their original topics.

## Events
### **What:** THEIA publishing fell behind real time as soon as we started it.
  * **Start time:** ~0900 ET
  * **End time:** ~1045 ET
  * **Impact:** We decided to switch topics and purge the data.
  * **Cause:** There was an unknown process using up 100% CPU on all three systems.  No one seems to know what that process is or think it ### **What:** What actually happened
  * **Start time:** Roughly when it started
  * **End time:** Roughly when it ended
  * **Impact:** How did it impact the engagement
  * **Cause:** What was the root cause (or closest thing to a root cause)
  * **Resolution:** How was the issue resolvedis important.
  * **Resolution:** The bad process was killed, the internal THEIA data was purged so we could start a fresh run, and the topic names were changed to `ta1-theia-X-e5-official-2`.

### **What:** The Five Directions topic `ta1-fivedirections-2-e5-official-1` stopped receiving Kafka messages, as a result of an unexpected `ta1-fivedirections-2` Windows host shutdown.
  * **Start time:** 12:46:29 PM (system went offline)
  * **End time:** ~1:40:00 PM (system back online and publishing)
  * **Impact:** TA5.1 background activity was paused and some minimal investigative work was performed to determine the cause of the shutdown.
  * **Cause:** Currently unknown, with suspicions that benign activity caused a system shutdown.
  * **Resolution:** The machine was rebooted with driver signing disabled using the command `BCDEDIT /set nointegritychecks ON`, and publishing resumed as normal. The system time was reset to UTC (4 hours behind) but was automatically reset to the current time in a matter of minutes. 

### **What:** The Marple topic `ta1-marple-1-e5-official-1` showed a noticeably diminished publishing rate, directly associated with the unexpected shutdown of the `ta1-marple-1` host.
  * **Start time:** 1:20:37 PM (system went offline)
  * **End time:** 1:25:37 PM (system reported online)
  * **Impact:** TA5.1 background activity was paused.
  * **Cause:** Currently unknown, with suspicions that benign activity caused a system shutdown.
  * **Resolution:** The target machine was rebooted with no noticeable side effects.

### **What:** THEIA's fine-grained recording was turned off
  * **Start time:** ~1545
  * **End time:** TBD
  * **Impact:** We manually turned off THEIA's fine-grained recording because it was causing issues with running Firefox.  The implication is that replay functionality will not work for THEIA from this point forward.  We will see if this can be resolved in future days to re-enable recording.  THEIA is currently investigating the issue on ta1-theia-target-3.tc.bbn.com.
  * **Cause:** TBD.  THEIA team is investigation.
  * **Resolution:** TBD.

### **What:** CADETS was publishing duplicate records today.
  * **Start time:** ~0900 ET
  * **End time:** ~1400 ET
  * **Impact:** Duplicate records were showing up in the `ta1-cadets-e5-official-1` topic.
  * **Cause:** There was an issue with the way the full distributed system had been started up.
  * **Resolution:** Some of the components in the distributed system were shut down to stop the duplication, and the full data set from the beginning of the day on was published to the new topic.  We switched the topic to start publishing all gathered data from the beginning of the data to `ta1-cadets-e5-official-2`.

# 2019-05-08

## Events
### **What:** MARPLE hosts started having publishing issues
  * **Start time:** ~0930 ET
  * **End time:** ~1130 ET
  * **Impact:** All three MARPLE hosts started to have various issues.  Some of them began publishing only UI events, and thus the publishing rates were very low, sometimes dropping to 0.  Some of them rebooted.  We attempted to restart publishing, and in some cases fully rebooted, some of the MARPLE systems.
  * **Cause:** it is unclear why any of this was happening.  Some of the reboots may have occurred due to bad interactions between our debugging and benign data generation.  We could not find any reason why only UI events were being logged in some cases.
  * **Resolution:** We restarted publishing for all of the MARPLE hosts repeatedly until the issues appeared to be resolved.

### **What:** ta1-theia-target-3 crashed
  * **Start time:** ~1130
  * **End time:** 2019-05-09 ~1110
  * **Impact:** How did it impact the engagement
  * **Cause:** THEIA resolved an issue with their fine-grained recording functionality yesterday, and they were testing that while we ran.  The cause of the crash is still TBD, but it is likely related to the fine-grained record/replay functionality.
  * **Resolution:** THEIA is currently investigating the crash.

### **What:** CADETS target systems had gaps in publishing
  * **Start time:** ~1145 ET
  * **End time:** 2019-05-09 ~0910 ET
  * **Impact:** One of the internal processes needed to be restarted for all CADETS hosts.  No data should be lost, however the particular host which had the process restarted had large gaps in publishing its CDM records.  Historical records were then published later on.
  * **Cause:** This internal process was known to need a restart.  In general, it will happen after hours, but today it needed to happen during the engagement.
  * **Resolution:** The internal process was restarted.

### **What:** The ta1-clearscope-2 phone stopped publishing
  * **Start time:** ~1245 ET
  * **End time:** ~1450 ET
  * **Impact:** Publishing was stopped and background activity was turned off
  * **Cause:** Part of the translator pipeline died.
  * **Resolution:** Things were restarted after some investigation.

# 2019-05-09
## Summary

## Events
### **What:** CADETS stoppoed publishing to their CDM topic
  * **Start time:** ~1005 ET
  * **End time:** ~1035 ET
  * **Impact:** Publishing was stopped for the CADETS CDM topic.
  * **Cause:** The CDM translator process had died.  The reason for this is unknown.
  * **Resolution:** The CDM translator is being restarted.

### **What:** ClearScope phone 1 was in a unresponsive state
  * **Start time:** ~1340 ET
  * **End time:** ~1520 ET
  * **Impact:** The phone was publishing at a very low rate, and the phone was unresponsive.
  * **Cause:** Something had gone wrong with the phone due to an application on the phone.  We are still investigating.
  * **Resolution:** The phone was rebooted and publishing was resumed to the same topic.

### **What:** TC test range reachability outage
  * **Start time:** ~1700
  * **End time:** ~1800
  * **Impact:** The TC test range and services and were totally unreachable.
  * **Cause:** A minor change of updating the secondary and tertiary DNS nameservers for a few key infrastructure machines was being deployed to work around a recent upstream network change that broke SMTP from our monitoring server, preventing alert email notifications.  While this change was being deployed, one of the interfaces with a publicly routable IP on the TC infrastructure went down and did not come back up due to an unknown operational issue.  Making matters worse, after this happened, the management software could no longer reach other systems in the TC test range.
  * **Resolution:** Once the downed interface was identified, it was manually brought back up and the original change was rolled back.

### **What:** CADETS publishing outage
  * **Start time:** ~1800
  * **End time:** 2019-05-10 ~1315
  * **Impact:** CADETS was not publishing any data to kafka.  Two of the three hosts started publishing to the CADETS stream again at ~0700 on 2019-05-10, but the other host was still not publishing.  Before that was resolved, the main CDM topic was not receiving any new records either.
  * **Cause:** An internal component of CADETS is being restarted on a nightly basis for stability purposes.  We expected this to result in a couple of hours of downtime after hours, but it appears to take much longer in the good case, and in some bad cases the host doesn't resume publishing without manual action.  In the case of the main CDM topic not getting records, the translator process had died.
  * **Resolution:** Manual action was taken on the third host to speed up its recovery.  After we detected that the translator had died, it was restarted.



# 2019-05-10
## Summary

## Events

### **What:** ta1-marple-3 not publishing data from all target monitoring components
  * **Start time:** ~1100 ET
  * **End time:** ~1400
  * **Impact:** The ta1-marple-3 system is only reporting a limited number of events (perhaps only UI events).
  * **Cause:** Unknown at this time.
  * **Resolution:** Publishing was restarted.

### **What:** THEIA publishing stopped for all three targets for a patch to be applied after engagement hours.
  * **Start time:** ~1730 ET
  * **End time:** ~2200 ET
  * **Impact:** All three THEIA hosts stopped publishing for a while and there may have been extra activity when they were publishing.
  * **Cause:** THEIA was applying a patch to fix their recording.
  * **Resolution:** The patch was applied and publishing was turned back on.


# 2019-05-13
## Summary

## Events

### **What:** ta1-marple-2 and ta1-marple-3 only publishing UI events
  * **Start time:** Over the weekend
  * **End Time:** ~0900 ET
  * **Impact:** Only UI events were being publishing for a couple of the MARPLE TA1 hosts.
  * **Cause:** Unknown, but this has been happening throughout the engagement.
  * **Resolution:** Publishing was restarted for both hosts.

### **What:** ta1-marple-2  only publishing UI events
  * **Start time:** ~0955 ET
  * **End Time:** ~1015 ET
  * **Impact:** Only UI events were being publishing for a ta1-marple-2.
  * **Cause:** Unknown, but this has been happening throughout the engagement.
  * **Resolution:** The host was rebooted and publishing was restarted.

### **What:** ta1-clearscope-2 phone stopped publishing
  * **Start time:** ~0710 ET
  * **End Time:** ~1010 ET
  * **Impact:** Publishing was stopped and background activity was turned off.
  * **Cause:** Something went wrong with the ch64 host/blade where it couldn't detect the phone. This is viewed as a hardware problem that isn't related to the phone. 
  * **Resolution:** Rebooted the host and restarted the translator.

### **What:** ta1-clearscope-3 phone will be taken out of engagement
  * **Start time:** ~1225 ET
  * **End Time:** None
  * **Impact:** The ta1-clearscope-3-e5-official-1 topic will no longer receive records.
  * **Cause:** We need to take a phone out of the engagement because we need a test device for diagnosing some of the issues we have been seeing, and we don't have any spares on hand.
  * **Resolution:** None


# 2019-05-14
## Summary

## Events

### **What:** ta1-fivedirections-1 was in a bad state
  * **Start time:** 2019-05-13 ~2000
  * **End time:** ~1800 ET
  * **Impact:** The ta1-fivedirections-1 target machine was shut down to allow for the data stream to catch up to real time.  It had drifted hours behind real time.
  * **Cause:** The system seemed to be overly bogged down from running for so long coupled with extensive background activity generation.
  * **Resolution:** The target was shut down, kept down to limit the amount of data being published, and once the publishing pipeline had caught back up to real time, the target was turned back on.

### **What:** ta1-fivedirections-2 was starting to get into a bad state
  * **Start time:** ~1500
  * **End time:** 2019-05-15 ~0930 ET
  * **Impact:** The ta1-fivedirections-2 target machine was shut down to allow for the data stream to catch up to real time.  It had drifted hours behind real time.
  * **Cause:** The system seemed to be overly bogged down from running for so long coupled with extensive background activity generation
  * **Resolution:** The target was shut down overnight, kept down to limit the amount of data being published.  We checked in late at night to turn the target back on after publishing had caught up to real time, however it wasn't clear at that point that the system had fully recovered.  We checked again in the morning, and the system had indeed recovered.

### **What:** ta1-clearscope-1 was frozen
  * **Start time:** ~1650 ET
  * **End time:** ~2030 ET; 2019-05-15 ~1130
  * **Impact:** The phone was frozen and unresponsive to touch.  It was still publishing, but it was not clear how valuable the published data was.
  * **Cause:** TBD
  * **Resolution:** The phone was rebooted at night to get it to be responsive again.  The next day, we had to manually fix up some issues to get the phone to be usable again.

# 2019-05-15
## Summary

## Events
### **What:** ta1-clearscope-2 is frozen
  * **Start time:** Noticed at ~0900 ET
  * **End time:** ~1250 ET
  * **Impact:** The phone is frozen and unresponsive to touch.
  * **Cause:** Unknown
  * **Resolution:** The phone was rebooted.

### **What:** ta1-marple-1 is down
  * **Start time:** ~1215 ET
  * **End time:** None
  * **Impact:** We will be keeping ta1-marple-1 down after it shut down during the engagement.
  * **Cause:** We've seen various problems across the three MARPLE TA1 hosts throughout the engagement, and trying to keep all three up at the same time with full publishing going has taken a bit of effort on both TA3 and TA5.1's part.  We've decided to keep at least 1 MARPLE TA1 host up from here on out, but we will not take any down proactively.  If it goes down or if the publishing gets messed up, then we will not bring it back up unless it is the last remaining TA1 MARPLE host.
  * **Resolution:** None

### **What:** THEIA publishing stopped and recording was turned off
  * **Start time:** ~1200 ET
  * **End time:** ~1430 ET
  * **Impact:** The hosts were having stability issues.  Publishing, logging, and recording was stopped on all hosts.  Recording was turned back on for all hosts around 1330 ET.  Publishing was turned on for the ta1-theia-target-3 host at around 1330 ET and it was turned on for the other two THEIA targets around 1430.  Note that logging and recording were running on the hosts which were not publishing, and they eventually pushed  all data out and caught back up to real time.
  * **Cause:** Various stability issues which seemed to go away after a reboot.
  * **Resolution:** Reboot and restarting publishing seemed to fix the issues.

### **What:** ta1-clearscope-2 is frozen
  * **Start time:** ~2355 05-15 ET
  * **End time:** ~1030 05-16 ET
  * **Impact:** The phone stopped publishing.
  * **Cause:** Unknown
  * **Resolution:** The phone was rebooted.

# 2019-05-16
## Summary

## Events

### **What:** ta1-marple-2 is down
  * **Start time:** 2019-05-15 ~1920 ET
  * **End time:** None
  * **Impact:** We will be keeping ta1-marple-2 down after it got into a bad publishing state last night.
  * **Cause:** We've seen various problems across the three MARPLE TA1 hosts throughout the engagement, and trying to keep all three up at the same time with full publishing going has taken a bit of effort on both TA3 and TA5.1's part.  We've decided to keep at least 1 MARPLE TA1 host up from here on out, but we will not take any down proactively.  If it goes down or if the publishing gets messed up, then we will not bring it back up unless it is the last remaining TA1 MARPLE host.
  * **Resolution:** None

### **What:** ta1-fivedirections-1 was bogged down
  * **Start time:** ~1445 ET
  * **End time:** ~1615 ET
  * **Impact:** The system was observed to be bogged down from host monitoring, and when we checked applications were not responding.
  * **Cause:** It is not clear at this time.  The system had grown unresponsive seemingly due to usage.
  * **Resolution:** The target was rebooted.

# 2019-05-17
## Summary

## Events