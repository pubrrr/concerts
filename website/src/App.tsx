import { ConcertList } from './ConcertList.tsx';
import { StyledLink } from './StyledLink.tsx';

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
            <p className='text-xs'>
                Emoji artwork is provided by <StyledLink href='https://emojitwo.github.io/'>Emojitwo</StyledLink>,
                originally released as <StyledLink href='https://www.emojione.com/'>Emojione 2.2</StyledLink> by{' '}
                <StyledLink href='http://www.ranks.com/'>Ranks.com</StyledLink> with contributions from the Emojitwo
                community and is licensed under{' '}
                <StyledLink href='https://creativecommons.org/licenses/by/4.0/legalcode'>CC-BY 4.0</StyledLink>.
            </p>
            <p className='text-xs'>
                Game icon artwork is provided by <StyledLink href='https://game-icons.net/'>Game Icons</StyledLink> and
                is licensed under{' '}
                <StyledLink href='https://creativecommons.org/licenses/by/3.0/legalcode'>CC-BY 3.0</StyledLink>.
            </p>
        </div>
    );
}

export default App;
