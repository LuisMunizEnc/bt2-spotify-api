export const API_CONFIG = {
    BASE_URL: 'http://127.0.0.1:8080',
    ENDPOINTS: {
      AUTH: {
        SPOTIFY_LOGIN: '/oauth2/authorization/spotify',
        LOGOUT: '/logout',
      },
      USER: {
        PROFILE: '/me',
        TOP_TRACKS: '/api/user/top-tracks',
        PLAYLISTS: '/api/user/playlists',
        RECENTLY_PLAYED: '/api/user/recently-played',
      },
    },
  };