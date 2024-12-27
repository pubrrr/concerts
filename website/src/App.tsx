import { ConcertList } from './ConcertList.tsx';
import { StyledLink } from './StyledLink.tsx';
import { GenreFilters, iconMap } from './types.ts';
import { FC } from 'react';

function App() {
    return (
        <div className='relative flex flex-col items-center text-center text-base-content'>
            <ScrollToTop />
            <div className='m-8 flex flex-col items-center gap-2'>
                <h1 className='mb-4 text-3xl font-bold text-primary'>All Concerts in Munich</h1>

                <p>Join the Telegram channels to get the newest updates:</p>
                <TelegramButton genre='metal' href='https://t.me/MunichMetalConcerts' />
                <TelegramButton genre='rock' href='https://t.me/MunichRockConcerts' />
                <TelegramButton genre='punk' href='https://t.me/MunichPunkConcerts' />
            </div>
            <ConcertList />
            <p className='mb-16 text-xs'>
                Emoji artwork is provided by <StyledLink href='https://emojitwo.github.io/'>Emojitwo</StyledLink>,
                originally released as <StyledLink href='https://www.emojione.com/'>Emojione 2.2</StyledLink> by{' '}
                <StyledLink href='http://www.ranks.com/'>Ranks.com</StyledLink> with contributions from the Emojitwo
                community and is licensed under{' '}
                <StyledLink href='https://creativecommons.org/licenses/by/4.0/legalcode'>CC-BY 4.0</StyledLink>. Game
                icon artwork is provided by <StyledLink href='https://game-icons.net/'>Game Icons</StyledLink> and is
                licensed under{' '}
                <StyledLink href='https://creativecommons.org/licenses/by/3.0/legalcode'>CC-BY 3.0</StyledLink>.
            </p>
        </div>
    );
}

const ScrollToTop = () => (
    <div className='absolute bottom-0 right-2 top-0 flex h-full items-end pt-[110vh] sm:hidden'>
        <a
            role='button'
            href='#'
            className='btn btn-circle btn-accent sticky bottom-4 z-[999]'
            aria-label='Scroll to top'
        >
            <span className='iconify text-2xl mdi--chevron-up' />
        </a>
    </div>
);

type TelegramButtonProps = {
    genre: keyof GenreFilters;
    href: string;
};

const TelegramButton: FC<TelegramButtonProps> = ({ genre, href }) => {
    return (
        <a role='button' className='btn btn-neutral btn-sm' href={href}>
            <span className={`iconify ${iconMap[genre]}`} />
            <span className='capitalize'>{genre} Channel</span>
        </a>
    );
};

export default App;
