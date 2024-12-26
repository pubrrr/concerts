import { ConcertList } from './ConcertList.tsx';

function App() {
    return (
        <div className='flex flex-col items-center'>
            <div className='m-8 flex flex-col items-center'>
                <h1 className='mb-4 text-3xl font-bold'>All Concerts in Munich</h1>

                <p>
                    <a className='link' href='https://t.me/MunichMetalConcerts'>
                        join the telegram METAL channel to get the newest updates
                    </a>
                </p>
                <p>
                    <a className='link' href='https://t.me/MunichRockConcerts'>
                        join the telegram ROCK channel to get the newest updates
                    </a>
                </p>
                <p>
                    <a className='link mb-4' href='https://t.me/MunichPunkConcerts'>
                        join the telegram PUNK channel to get the newest updates
                    </a>
                </p>
            </div>
            <ConcertList />
        </div>
    );
}

export default App;
