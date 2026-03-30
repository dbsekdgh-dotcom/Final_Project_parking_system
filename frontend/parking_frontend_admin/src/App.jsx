import Header from './shared/components/header/Header'
import Sidebar from './shared/components/sidebar/Sidebar'
import './App.css'
function App() {
 

  return (
    <div className="layout">
      <Sidebar />
      <div className="layout__main">
        <Header />
        <main className="layout__content" aria-label="콘텐츠 영역" />
      </div>
    </div>
  )
}

export default App
