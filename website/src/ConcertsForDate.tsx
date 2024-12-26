import { Concert, GenreFilters } from './types.ts';
import ConcertItem from './ConcertItem.tsx';
import { FC, useMemo } from 'react';
import { matchesGenre } from './matchesGenre.ts';

type ConcertsForDateProps = {
    date: string;
    concerts: Concert[];
    filters: GenreFilters;
};

export const ConcertsForDate: FC<ConcertsForDateProps> = ({ date, concerts, filters }) => {
    const filteredConcerts = useMemo(
        () => concerts.filter((concert) => matchesGenre(concert, filters)),
        [concerts, filters],
    );

    if (filteredConcerts.length === 0) {
        return null;
    }

    return (
        <>
            <thead>
                <tr className='sticky top-0 text-primary'>
                    <th className='p-4'>{date}</th>
                </tr>
            </thead>
            <tbody>
                {filteredConcerts.map((concert, index) => (
                    <ConcertItem key={index} concert={concert} />
                ))}
            </tbody>
        </>
    );
};
