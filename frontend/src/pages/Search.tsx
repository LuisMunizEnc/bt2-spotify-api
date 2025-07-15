import { useEffect } from 'react';
import { TrackCard } from '../components/TrackCard';
import { Button } from '../components/ui/Button'
import type { Album, Artist, Playlist, SpotifyImage, Track } from '../types';
import { AlbumCard } from '../components/AlbumCard';
import { ArtistCard } from '../components/ArtistCard';
import { PlaylistCard } from '../components/PlaylistCard';

function SearchPage() {

  useEffect(() => {
    console.log(generateRandomTrack());
  });

  const randomId = () => Math.random().toString(36).substring(2, 10);
  
  const randomImage = (): SpotifyImage => ({
    url: `https://picsum.photos/seed/${randomId()}/300/300`,
    width: 300,
    height: 300
  });

  const randomArtist = (): Artist => ({
    id: randomId(),
    name: `Artist ${Math.floor(Math.random() * 1000)}`,
    images: [randomImage()],
    externalURLs: {
      spotify: `https://open.spotify.com/album/${randomId()}`
    },
  });

  const randomPlaylist = (): Playlist => ({
    id: randomId(),
    description: "This is a placeholder description",
    name: "Playlist Name",
    images: [randomImage()],
    owner: {
        displayName: "Owner"
    },
    externalURLs: {
        spotify: `https://open.spotify.com/album/${randomId()}`
    },
    tracks: []
  });

  function randomAlbum(): Album {
    return {
      id: randomId(),
      albumType: "album",
      totalTracks: `${Math.floor(Math.random() * 15 + 1)}`,
      externalURLs: {
        spotify: `https://open.spotify.com/album/${randomId()}`
      },
      images: [randomImage()],
      name: `Album ${Math.floor(Math.random() * 1000)}`,
      artists: [randomArtist()],
      releaseDate: "12/12/12",
      tracks: []
    }
  }

  function generateRandomTrack(): Track {
    return {
      id: randomId(),
      album: randomAlbum(),
      artists: [randomArtist()],
      durationMs: Math.floor(Math.random() * 300000), 
      name: `Track ${Math.floor(Math.random() * 10000)}`,
      popularity: Math.floor(Math.random() * 100),
      previewURL: `https://p.scdn.co/mp3-preview/${randomId()}`,
      isPlayable: Math.random() > 0.1
    };
  }

  return (
    <>
      <h1>Vite + React for Spotify API</h1>
      <div className="card">
        <h2>
          Components:
        </h2>
        <div className='flex content-between justify-center items-center gap-5 my-10'>
          <span>Button for search:</span>
          <Button loading={false} size='sm'>Search</Button>
        </div>
        <hr />
        <div className='flex flex-col content-between justify-center items-center gap-5 my-10'>
          <span>Track card:</span>
          <TrackCard track={generateRandomTrack()} index={0}/>
          <TrackCard track={generateRandomTrack()} index={1}/>
        </div>
        <hr/>
        <div className='flex flex-col content-between justify-center items-center gap-5 my-10'>
          <span>Album card:</span>
          <AlbumCard album={randomAlbum()}/>
        </div>
        <hr/>
        <div className='flex flex-col content-between justify-center items-center gap-5 my-10'>
          <span>Artist card:</span>
          <ArtistCard artist={randomArtist()}/>
        </div>
        <hr/>
        <div className='flex flex-col content-between justify-center items-center gap-5 my-10'>
          <span>Playlist card:</span>
          <PlaylistCard playlist={randomPlaylist()}/>
        </div>
      </div>
      <p className="read-the-docs">
        End of ui. Let's work
      </p>
    </>
  )
}

export default SearchPage
