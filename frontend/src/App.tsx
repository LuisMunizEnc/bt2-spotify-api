import './App.css'
import { Button } from './components/ui/Button'

function App() {

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
        <hr/>
        <div className='flex flex-col content-between justify-center items-center gap-5 my-10'>
          <span>Track card:</span>
          <Button loading={false} size='sm'>Search</Button>
        </div>
        <hr/>
      </div>
      <p className="read-the-docs">
        End of ui
      </p>
    </>
  )
}

export default App
