package zet.kedzieri.usosztauth.web.protocol;

import java.util.UUID;

/**
 * @param sessionUuid      łańcuch oznaczający sesję użytkownika po udanym zalogowaniu
 * @param authUrl          odnośnik przekierowywujący do uwierzytelnienia przez USOS
 */
public record AuthInitResponse(UUID sessionUuid, String authUrl) {}
