package com.comeon.websocket.web.message.controller;

public interface LockedMeetingPlaceProvider {

    LockedMeetingPlaceListResponse getLockedMeetingPlaceList(Long meetingId);
}
